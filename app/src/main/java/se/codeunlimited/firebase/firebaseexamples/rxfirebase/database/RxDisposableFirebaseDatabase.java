package se.codeunlimited.firebase.firebaseexamples.rxfirebase.database;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Wrapper class for RxFirebaseDatabase which will add each subsription to a list of disposables and
 * allow you to remove them selectively or all at once
 */
public final class RxDisposableFirebaseDatabase {
    private static final String TAG = "RxDisposableFBDB";

    private final RxFirebaseDatabase rxFirebaseDatabase;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Map<String, Disposable> managedDisposables = new HashMap<>();

    public RxDisposableFirebaseDatabase(@NonNull String url) {
        rxFirebaseDatabase = new RxFirebaseDatabase(url);
    }

    @NonNull
    public Disposable setValue(@NonNull String path, @NonNull Object obj, Consumer<DatabaseReference> observer, Consumer<Throwable> onError) {
        Disposable disposable = rxFirebaseDatabase.setValue(path, obj).subscribe(observer, onError);
        disposables.add(disposable);
        return disposable;
    }

    @NonNull
    public String push(String path) {
        return rxFirebaseDatabase.push(path);
    }

    @NonNull
    public Disposable updateChildren(@NonNull Map<String, Object> childUpdates, Consumer<DatabaseReference> observer, Consumer<Throwable> onError) {
        Disposable disposable = rxFirebaseDatabase.updateChildren(childUpdates).subscribe(observer, onError);
        disposables.add(disposable);
        return disposable;
    }

    public Builder builder(@NonNull String path) {
        return new Builder(path);
    }

    public interface Matcher {
        boolean matches(@NonNull String name);
    }

    public void removeListeners(Matcher matcher) {
        Log.d(TAG, "removeListeners");
        Set<String> keys = new TreeSet<>(managedDisposables.keySet());
        for (String child : keys) {
            if (matcher.matches(child)) {
                Disposable disposable = managedDisposables.remove(child);
                disposables.remove(disposable);
                break;
            }
        }
    }

    public void clearDisposables() {
        Log.d(TAG, "clearDisposables");
        disposables.clear();
    }

    public class Builder {
        private final String path;
        private final RxFirebaseDatabase.Builder builder;

        private Builder(@NonNull String path) {
            this.path = path;
            this.builder = rxFirebaseDatabase.builder(path);
        }

        @NonNull
        public Builder delay(long ms) {
            builder.delay(ms);
            return this;
        }

        @NonNull
        public <T> Disposable observe(@NonNull Class<T> clazz, Consumer<Optional<T>> observer) {
            Disposable disposable = builder.observe(clazz).subscribe(observer);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public <T> Disposable observe(@NonNull Class<T> clazz, Consumer<Optional<T>> observer, Consumer<Throwable> onError) {
            Disposable disposable = builder.observe(clazz).subscribe(observer, onError);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public <T> Disposable observeChildren(@NonNull Class<T> clazz, Consumer<List<T>> observer) {
            Disposable disposable = builder.observeChildren(clazz).subscribe(observer);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public <T> Disposable observeChildren(@NonNull Class<T> clazz, Consumer<List<T>> observer, Consumer<Throwable> onError) {
            Disposable disposable = builder.observeChildren(clazz).subscribe(observer, onError);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public Disposable single(Consumer<DataSnapshot> observer) {
            Disposable disposable = builder.single().subscribe(observer);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public Disposable single(Consumer<DataSnapshot> observer, Consumer<Throwable> onError) {
            Disposable disposable = builder.single().subscribe(observer, onError);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public <T> Disposable single(@NonNull Class<T> clazz, Consumer<Optional<T>> observer) {
            Disposable disposable = builder.single(clazz).subscribe(observer);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public <T> Disposable single(@NonNull Class<T> clazz, Consumer<Optional<T>> observer, Consumer<Throwable> onError) {
            Disposable disposable = builder.single(clazz).subscribe(observer, onError);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public <T> Disposable singleChildren(@NonNull Class<T> clazz, Consumer<List<T>> observer) {
            Disposable disposable = builder.singleChildren(clazz).subscribe(observer);
            addDisposable(disposable);
            return disposable;
        }

        @NonNull
        public <T> Disposable singleChildren(@NonNull Class<T> clazz, Consumer<List<T>> observer, Consumer<Throwable> onError) {
            Disposable disposable = builder.singleChildren(clazz).subscribe(observer, onError);
            addDisposable(disposable);
            return disposable;
        }

        private void addDisposable(Disposable disposable) {
            Log.d(TAG, "addDisposable: " + path);
            if (managedDisposables.containsKey(path)) {
                Disposable old = managedDisposables.remove(path);
                disposables.remove(old);
            }
            managedDisposables.put(path, disposable);
            disposables.add(disposable);
        }
    }
}
