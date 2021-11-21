package pro.guopi.tidy.wish

import pro.guopi.tidy.YSubscriber
import pro.guopi.tidy.YWish

class WishCreate<T>(
    val creator: ()
) : YWish<T> {
    override fun subscribe(ys: YSubscriber<T>) {
        TODO("Not yet implemented")
    }
}