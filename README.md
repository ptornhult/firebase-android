# Firebase Android Demo

This project is a result of my experiments with firebase and other somewhat related projects. Since
I had some problems setting everything up, maybe this will help someone to see how things can fit
together. This is not meant to be a library project of its own, but rather a demo/example of how you
can structure your android/firebase project.

## Libraries I've added to this demo so far
- Google Firebase Database
  https://firebase.google.com/
- Google Firebase Auth
  https://firebase.google.com/
- RxJava 2
  (to be able to subscribe to firebase events)
  https://github.com/ReactiveX/RxJava
- Google AutoValue
  (to force immutable objects and simplify the code a lot)
  https://github.com/google/auto/blob/master/value/userguide/index.md
- auto-value-parcel
  (Automagically generate parcelable code for AutoValue)
  https://github.com/rharter/auto-value-parcel
- auto-value-firebase
  (Makes AutoValue work for Firebase)
  https://github.com/mattlogan/auto-value-firebase
- Retrolambda
  (Allows for some Java8 functionality in Android: Lambda support)
  https://github.com/evant/gradle-retrolambda
- Butterknife
  (Awesome library to reduce your Android view code) 
  http://jakewharton.github.io/butterknife/
- Icepick
  (Another awesome library to easily manage some of your Android lifcycle state)
  https://github.com/frankiesardo/icepick