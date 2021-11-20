import pro.guopi.tidy.Y

fun fromOtherThread() {
    //...
    Y.runInMainPlane {
        //...
    }
}