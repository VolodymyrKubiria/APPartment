package com.unison.appartment.repository;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unison.appartment.database.DatabaseConstants;
import com.unison.appartment.database.StorageConstants;
import com.unison.appartment.livedata.FirebaseQueryLiveData;
import com.unison.appartment.model.Post;
import com.unison.appartment.model.UserHome;
import com.unison.appartment.state.Appartment;
import com.unison.appartment.state.MyApplication;
import com.unison.appartment.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostRepository {
    // Nodo del database a cui sono interessato
    private DatabaseReference postRef;
    // Livedata che rappresenta i dati nel nodo del database considerato che vengono convertiti
    // tramite un Deserializer in ogetti di tipo UncompletedTask
    private FirebaseQueryLiveData liveData;
    private LiveData<List<Post>> postLiveData;

    private MutableLiveData<Boolean> loading;

    public PostRepository() {
        // Riferimento al nodo del Database interessato (i task non completati della casa corrente)
        postRef =
                FirebaseDatabase.getInstance().getReference(
                        DatabaseConstants.SEPARATOR + DatabaseConstants.POSTS +
                                DatabaseConstants.SEPARATOR + Appartment.getInstance().getHome().getName());
        Query orderedPosts = postRef.orderByChild(DatabaseConstants.POSTS_HOMENAME_POSTID_TIMESTAMP);
        liveData = new FirebaseQueryLiveData(orderedPosts);
        postLiveData = Transformations.map(liveData, new PostRepository.Deserializer());

        loading = new MutableLiveData<>();
    }

    @NonNull
    public LiveData<List<Post>> getPostLiveData() {
        return postLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loading;
    }

    public void addPost(Post newPost) {
        // Segnalo l'inizio del caricamento
        loading.setValue(true);

        switch (newPost.getType()){
            case Post.TEXT_POST:
                // Nel caso del testo il post non deve essere modificato
                addUpdatedPost(newPost);
                break;
            case Post.IMAGE_POST:
                addImagePost(newPost);
                break;
            case Post.AUDIO_POST:
                addAudioPost(newPost);
                break;
        }
    }

    public void addImagePost(final Post newPost) {
        Glide.with(MyApplication.getAppContext()).asBitmap().load(newPost.getContent()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                // Ridemnsiono l'immagine (in generale la rimpicciolisco)
                resource = ImageUtils.resize(resource, ImageUtils.MAX_WIDTH, ImageUtils.MAX_HEIGHT);
                // Comprimo l'immagine
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resource.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] data = baos.toByteArray();

                // UUID genera un nome univoco per il file che sto caricando
                final StorageReference postImageRef = FirebaseStorage.getInstance().getReference().child(StorageConstants.POST_IMAGES).
                        child(Appartment.getInstance().getHome().getName()).child(UUID.randomUUID().toString());
                final String storagePath = postImageRef.getPath();
                UploadTask uploadTask = postImageRef.putBytes(data);

                // Codice della guida per ottenere l'URL di download del media appena caricato
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            // TODO gestire errore upload
                        }
                        return postImageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String imageUrl = task.getResult().toString();
                            newPost.setContent(imageUrl);
                            newPost.setStoragePath(storagePath);
                            addUpdatedPost(newPost);
                        } else {
                            // TODO gestire errore upload
                        }
                    }
                });
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    public void addAudioPost(final Post newPost) {
        final StorageReference postAudioRef = FirebaseStorage.getInstance().getReference().child(StorageConstants.POST_AUDIOS)
                .child(Appartment.getInstance().getHome().getName()).child(UUID.randomUUID().toString());
        final String storagePath = postAudioRef.getPath();
        Uri uri = Uri.fromFile(new File(newPost.getContent()));
        UploadTask uploadTask = postAudioRef.putFile(uri);

        // Codice della guida per ottenere l'URL di download del media appena caricato
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    // TODO gestire errore upload
                }
                return postAudioRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    String audioUrl = task.getResult().toString();
                    newPost.setContent(audioUrl);
                    newPost.setStoragePath(storagePath);
                    addUpdatedPost(newPost);
                } else {
                    // TODO gestire errore upload
                }
            }
        });
    }

    private void addUpdatedPost(Post newPost) {
        String key = postRef.push().getKey();
        newPost.setId(key);
        newPost.setTimestamp((-1) * newPost.getTimestamp());
        postRef.child(key).setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Segnalo la fine del caricamento
                loading.setValue(false);
            }
        });
    }

    public void deletePost(Post post) {
        // Elimino anche il media memorizzato nello storage associato al post, se c'è
        if (post.getStoragePath() != null) {
            StorageReference postRef = FirebaseStorage.getInstance().getReference(post.getStoragePath());
            postRef.delete();
        }
        postRef.child(post.getId()).removeValue();
    }

    private class Deserializer implements Function<DataSnapshot, List<Post>> {
        @Override
        public List<Post> apply(DataSnapshot dataSnapshot) {
            List<Post> posts = new ArrayList<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                Post post = postSnapshot.getValue(Post.class);
                post.setId(postSnapshot.getKey());
                post.setTimestamp((-1) * post.getTimestamp());
                posts.add(post);
            }
            return posts;
        }
    }
}
