package se.codeunlimited.firebase.firebaseexamples.rxfirebase.auth;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;

public final class RxFirebaseAuth {

    private final FirebaseAuth firebaseAuth;

    public RxFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public Observable<FirebaseAuth> authenticationState() {
        return Observable.create(new ObservableOnSubscribe<FirebaseAuth>() {
            @Override
            public void subscribe(ObservableEmitter<FirebaseAuth> emitter) throws Exception {
                emitter.onNext(firebaseAuth);
                FirebaseAuthStateListener listener = new FirebaseAuthStateListener(emitter);
                emitter.setCancellable(() -> firebaseAuth.removeAuthStateListener(listener));
                firebaseAuth.addAuthStateListener(listener);
            }
        });
    }

    public void signout() {
        firebaseAuth.signOut();
    }

    public Single<FirebaseUser> signInWithEmailAndPassword(String email, String password) {
        return signIn(firebaseAuth.signInWithEmailAndPassword(email, password));
    }

    public Single<FirebaseUser> signInWithCustomToken(String token) {
        return signIn(firebaseAuth.signInWithCustomToken(token));
    }

    public Single<FirebaseUser> signInAnonymously() {
        return signIn(firebaseAuth.signInAnonymously());
    }

    public Single<FirebaseUser> signInWithCredential(AuthCredential credential) {
        return signIn(firebaseAuth.signInWithCredential(credential));
    }

    private Single<FirebaseUser> signIn(Task<AuthResult> signin) {
        return Single.create(emitter -> signin
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!emitter.isDisposed()) {
                            emitter.onSuccess(task.getResult().getUser());
                        }
                    } else {
                        Throwable error = task.getException();
                        if (error == null) {
                            error = new FirebaseAuthException("Unknown Authentication Problem",
                                    "Login returned unsuccessful!");
                        }
                        if (!emitter.isDisposed()) {
                            emitter.onError(error);
                        }
                    }
                }));
    }


    private static class FirebaseAuthStateListener implements FirebaseAuth.AuthStateListener {
        private final Emitter<FirebaseAuth> emitter;

        FirebaseAuthStateListener(Emitter<FirebaseAuth> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            emitter.onNext(firebaseAuth);
        }
    }
}
