package idv.wennyli.workoutlog.utils

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

interface AppResource {
    fun resources(): Resources
    fun getString(stringRes: Int): String
    fun getString(stringRes: Int, vararg formatArgs: Any): String
    fun getStringArray(stringArrayRes: Int): Array<String>
    fun getColor(@ColorRes colorRes: Int): Int
    fun getDrawable(@DrawableRes drawableRes: Int): Drawable?
    fun getAsset(): AssetManager
}

class ResourceProvider(
    private val context: Context
) : AppResource {

    override fun resources(): Resources = context.resources

    override fun getString(stringRes: Int): String = context.getString(stringRes)

    override fun getString(stringRes: Int, vararg formatArgs: Any): String =
        context.getString(stringRes, *formatArgs)

    override fun getStringArray(stringArrayRes: Int): Array<String> =
        context.resources.getStringArray(stringArrayRes)

    override fun getColor(@ColorRes colorRes: Int): Int =
        ContextCompat.getColor(context, colorRes)

    override fun getDrawable(@DrawableRes drawableRes: Int): Drawable? =
        ContextCompat.getDrawable(context, drawableRes)

    override fun getAsset(): AssetManager = context.assets
}
