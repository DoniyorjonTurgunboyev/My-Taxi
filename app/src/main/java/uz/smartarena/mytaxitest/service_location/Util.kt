import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings

object Util {
    fun showAlertLocation(context: Context, title: String, message: String, btnText: String) {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(btnText) { dialog, _ ->
            dialog.dismiss()
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        alertDialog.show()
    }
}