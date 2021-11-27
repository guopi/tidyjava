import pro.guopi.tidy.Tidy

fun fromOtherThread() {
    //...
    Tidy.runInMainPlane {
        //...
    }
}