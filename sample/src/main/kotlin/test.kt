import pro.guopi.tidy.Tidy.Companion.main

fun fromOtherThread() {
    //...
    main.start {
//...
    }
}