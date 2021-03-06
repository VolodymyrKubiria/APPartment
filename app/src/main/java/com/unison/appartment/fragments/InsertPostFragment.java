package com.unison.appartment.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.snackbar.Snackbar;
import com.unison.appartment.R;
import com.unison.appartment.model.Post;

import java.io.IOException;

/**
 * Le Activity che contengono questo fragment devono implementare l'interface
 * {@link OnInsertPostFragmentListener} per gestire gli eventi di interazione
 */
public class InsertPostFragment extends Fragment {

    private static final int PERMISSION_REQUEST_RECORDER = 1;

    private final static String BUNDLE_KEY_LOADED = "loaded";

    // Edittext contenente l'input del messaggio
    private EditText inputText;

    // Request code per aprire l'activity usata per caricare un'immagine
    private static int RESULT_LOAD_IMAGE = 1;
    // Listener usato per la gestione degli eventi interni al fragment
    private OnInsertPostFragmentListener listener;
    // Oggetto usato per la registrazione di audio
    private MediaRecorder recorder;
    // Flag usato per monitorare se è in corso una registrazione
    private boolean isRecording = false;
    // Nome del file in cui viene salvato l'audio
    private String fileName;

    private ImageButton btnSendText;
    private ImageButton btnSendImg;
    private ImageButton btnSendAudio;

    /**
     * Costruttore vuoto obbligatorio che viene usato nella creazione del fragment
     */
    public InsertPostFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InsertPostFragment.
     */
    public static InsertPostFragment newInstance() {
        return new InsertPostFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View myView =  inflater.inflate(R.layout.fragment_insert_post, container, false);

        if (savedInstanceState == null) {
            loaded = false;
        } else {
            loaded = savedInstanceState.getBoolean(BUNDLE_KEY_LOADED);
        }

        btnSendText = myView.findViewById(R.id.fragment_insert_post_btn_send_text);
        btnSendText.setEnabled(false);
        btnSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    EditText inputText = myView.findViewById(R.id.fragment_insert_post_input_text);
                    listener.onInsertPostFragmentSendPost(inputText.getText().toString(), Post.TEXT_POST);
                    // Ripulisco l'edit text dopo che il messaggio è stato inviato
                    inputText.setText("");
                }
            }
        });

        btnSendImg = myView.findViewById(R.id.fragment_insert_post_btn_send_img);
        btnSendImg.setEnabled(false);
        btnSendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                            RESULT_LOAD_IMAGE);
                }
            }
        });

        btnSendAudio = myView.findViewById(R.id.fragment_insert_post_btn_send_audio);
        btnSendAudio.setEnabled(false);
        btnSendAudio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    // Controllo di avere il permesso di registrare
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Non ho il permesso di registrare, quindi lo richiedo
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_REQUEST_RECORDER);
                    } else {
                        // Ho il permesso di registrare
                        isRecording = true;
                        // Metto il focus sul bottone di registrazione
                        btnSendAudio.setFocusableInTouchMode(true);
                        btnSendAudio.requestFocus();
                        // Disabilito tutti i campi che non siano il registratore
                        inputText.setEnabled(false);
                        btnSendText.setEnabled(false);
                        btnSendImg.setEnabled(false);
                        // Scrivo nel campo di testo che è in corso la registrazione
                        inputText.setText(R.string.fragment_insert_post_text_recording);
                        startRecording();
                    }
                }
                return true;
            }
        });
        btnSendAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    isRecording = false;
                    stopRecording();
                    // Rimuovo il focus dal bottone di registrazione
                    btnSendAudio.clearFocus();
                    // Rimuovo dal campo di testo che è in corso la registrazione
                    inputText.getText().clear();
                    // Riabilito i campi al termine della registrazione
                    inputText.setEnabled(true);
                    btnSendImg.setEnabled(true);
                    // Una volta terminata la registrazione dell'audio aggiungo il post
                    listener.onInsertPostFragmentSendPost(fileName, Post.AUDIO_POST);
                } else {
                    // Se non si sta registrando e si preme sul bottone mostro un avviso su come
                    // registrare un audio attraverso l'uso di uno snackbar
                    View snackbarView = getActivity().findViewById(R.id.fragment_messages);
                    /*
                    ATTENZIONE: Lo snackbar ha bisogno di un riferimento ad una parent view per
                    essere visualizzato; in questo caso viene preso un riferimento ad un layout
                    che dovrebbe essere sicuramente visualizzato ma comunque è ESTERNO a questo
                    fragment.
                     */
                    Snackbar.make(snackbarView, getString(R.string.fragment_insert_post_btn_audio_suggestion),
                            Snackbar.LENGTH_LONG).show();
                }
                btnSendAudio.setFocusableInTouchMode(false);
            }
        });
        // Cambio il colore del bottone di invio del testo in base al fatto che il campo di input
        // sia riempito o meno
        inputText = myView.findViewById(R.id.fragment_insert_post_input_text);
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Il campo di testo è considerato vuoto se contiene solo spazi o \n oppure
                // se si sta registrando (in quel caso c'è del testo, ma non deve poter essere inviato)
                if (s.toString().replaceAll("\n", "")
                                .replaceAll("\\s+", "").length() == 0 || isRecording) {
                    // btnSendText.setColorFilter(ContextCompat.getColor(getActivity(), R.color.gray));
                    btnSendText.setEnabled(false);
                } else {
                    // btnSendText.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                    // Se non si è ancora caricata la lista dei messaggi non consento la pubblicazione di un post
                    if (loaded) {
                        btnSendText.setEnabled(true);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return myView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(BUNDLE_KEY_LOADED, loaded);

        super.onSaveInstanceState(outState);
    }

    private boolean loaded = false;
    public void loaded() {
        this.loaded = true;
        btnSendText.setEnabled(inputText.getText().toString().replaceAll("\n", "")
                .replaceAll("\\s+", "").length() != 0 );
        btnSendImg.setEnabled(true);
        btnSendAudio.setEnabled(true);
    }

    /**
     * Recupero dell'immagine selezionata dall'utente
     * @param requestCode il codice della richiesta
     * @param resultCode il codice del risultato
     * @param data i dati restituiti dall'activity chiamata
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            listener.onInsertPostFragmentSendPost(selectedImage.toString(), Post.IMAGE_POST);
        }
    }

    /**
     * Recupero i permessi per la registrazione di audio
     * @param requestCode il codice della richiesta
     * @param permissions i permessi richiesti
     * @param grantResults i risultati ottenuti
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORDER: {
                // Se la richiesta è cancellata l'array dei risultati è vuoto
                if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Il permesso è stato fornito, posso effettuare la registrazione
                    // Non faccio nulla perché per iniziare la registrazione deve tenere premuto
                } else {
                    // Il permesso è stato negato, non effettuo la registrazione
                }
                break;
            }
            // Altri CASE se l'applicazione richiede anche altri permessi
        }
    }

    /**
     * Inizio la registrazione
     */
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        fileName = getActivity().getExternalCacheDir().getAbsolutePath() + "/audiotest.3gp";
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            // Qualcosa è andato storto con la registrazione
        }
        recorder.start();
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnInsertPostFragmentListener) {
            listener = (OnInsertPostFragmentListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInsertPostFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Questa interfaccia deve essere implementata dalle activity che contengono questo
     * fragment, per consentire al fragment di comunicare eventuali interazioni all'activity
     * che a sua volta può comunicare con altri fragment
     */
    public interface OnInsertPostFragmentListener {
        void onInsertPostFragmentSendPost(String content, int postType);
    }
}
