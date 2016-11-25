package se.codeunlimited.firebase.firebaseexamples.rxfirebase.database;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * FirebaseDatabase provided using RxJava Observables
 */
public class RxFirebaseDatabase {

    private static final String TAG = "RxFirebaseDatabase";

    private final DatabaseReference databaseReference;

    public RxFirebaseDatabase(@NonNull String url) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        this.databaseReference = firebaseDatabase.getReferenceFromUrl(url);
    }

    public DatabaseReference getDatabaseReference(String path) {
        return databaseReference.child(path);
    }

    @NonNull
    public Single<DatabaseReference> setValue(@NonNull String path, @NonNull Object obj) {
        return Single.create(emitter -> {
            DatabaseReference reference = getDatabaseReference(path);
            reference.setValue(obj, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(databaseReference);
                    }
                } else {
                    Log.e(TAG, "Error", databaseError.toException());
                    if (!emitter.isDisposed()) {
                        emitter.onError(databaseError.toException());
                    }
                }
            });
        });
    }

    @NonNull
    public String push(String path) {
        return getDatabaseReference(path).push().getKey();
    }

    @NonNull
    public Single<DatabaseReference> updateChildren(@NonNull Map<String, Object> childUpdates) {
        return Single.create(emitter -> databaseReference.updateChildren(childUpdates, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                if (!emitter.isDisposed()) {
                    emitter.onSuccess(databaseReference);
                }
            } else {
                Log.e(TAG, "Error", databaseError.toException());
                if (!emitter.isDisposed()) {
                    emitter.onError(databaseError.toException());
                }
            }
        }));
    }

    public Builder builder(@NonNull String path) {
        return new RxFirebaseDatabase.Builder(path);
    }

    public class Builder {
        private final String path;
        private long delayMillis = 0;

        private Builder(@NonNull String path) {
            this.path = path;
        }

        @NonNull
        public Builder delay(long ms) {
            delayMillis = ms;
            return this;
        }

        @NonNull
        private Observable<DataSnapshot> observe() {
            Observable<DataSnapshot> observable = Observable.create(emitter -> {
                ValueEventListener listener = getValueEventListener(emitter);
                DatabaseReference reference = getDatabaseReference(path);
                emitter.setCancellable(() -> reference.removeEventListener(listener));
                reference.addValueEventListener(listener);
            });

            if (delayMillis > 0) {
                return observable.delay(delayMillis, TimeUnit.MILLISECONDS);
            }

            return observable;
        }

        @NonNull
        public <T> Observable<Optional<T>> observe(@NonNull Class<T> clazz) {
            return observe()
                    .observeOn(Schedulers.computation())
                    .map(snapshot -> mapToOptional(snapshot, clazz))
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @NonNull
        public <T> Observable<List<T>> observeChildren(@NonNull Class<T> clazz) {
            return observe()
                    .observeOn(Schedulers.computation())
                    .map(snapshot -> mapChildrenToList(snapshot, clazz))
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @NonNull
        private ValueEventListener getValueEventListener(final ObservableEmitter<DataSnapshot> emitter) {
            return new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(dataSnapshot);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error", databaseError.toException());
                    if (!emitter.isDisposed()) {
                        emitter.onError(databaseError.toException());
                    }
                }
            };
        }

        @NonNull
        public Single<DataSnapshot> single() {
            Single<DataSnapshot> single = Single.create(emitter -> {
                ValueEventListener listener = getValueEventListener(emitter);
                DatabaseReference reference = getDatabaseReference(path);
                emitter.setCancellable(() -> reference.removeEventListener(listener));
                reference.addValueEventListener(listener);
            });

            if (delayMillis > 0) {
                return single.delay(delayMillis, TimeUnit.MILLISECONDS);
            }

            return single;
        }

        @NonNull
        public <T> Single<Optional<T>> single(@NonNull Class<T> clazz) {
            return single()
                    .observeOn(Schedulers.computation())
                    .map(snapshot -> mapToOptional(snapshot, clazz))
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @NonNull
        public <T> Single<List<T>> singleChildren(@NonNull Class<T> clazz) {
            return single()
                    .observeOn(Schedulers.computation())
                    .map(snapshot -> mapChildrenToList(snapshot, clazz))
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @NonNull
        private ValueEventListener getValueEventListener(final SingleEmitter<DataSnapshot> emitter) {
            return new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(dataSnapshot);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error", databaseError.toException());
                    if (!emitter.isDisposed()) {
                        emitter.onError(databaseError.toException());
                    }
                }
            };
        }

        private <T> T create(Class<T> clazz, DataSnapshot dataSnapshot) {
            try {
                Method builder = clazz.getMethod("builder", DataSnapshot.class);
                Object obj = builder.invoke(null, dataSnapshot);
                Method build = obj.getClass().getMethod("build");
                //noinspection unchecked
                return (T) build.invoke(obj, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressWarnings("Guava")
        private <T> Optional<T> mapToOptional(DataSnapshot snapshot, Class<T> clazz) {
            Log.v(TAG, "Loading (" + path + "): " + snapshot);
            T obj = create(clazz, snapshot);
            if (obj != null) {
                return Optional.of(obj);
            } else {
                return Optional.absent();
            }
        }

        private <T> List<T> mapChildrenToList(DataSnapshot snapshot, Class<T> clazz) {
            Log.v(TAG, "Loading (" + path + ") children: " + snapshot.getChildrenCount());
            List<T> items = new ArrayList<>((int)snapshot.getChildrenCount());
            for (DataSnapshot ds : snapshot.getChildren()) {
                items.add(create(clazz, ds));
            }
            return items;
        }
    }
}
