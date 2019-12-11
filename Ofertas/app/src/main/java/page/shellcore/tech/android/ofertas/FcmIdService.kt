package page.shellcore.tech.android.ofertas

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class FcmIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        val refreshToken = FirebaseInstanceId.getInstance()
            .token

        sendRegistrationToServer(refreshToken!!)
    }

    private fun sendRegistrationToServer(token: String) {
        Log.d("Token", token)
    }
}
