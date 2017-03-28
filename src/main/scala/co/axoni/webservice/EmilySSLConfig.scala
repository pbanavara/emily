package co.axoni.webservice

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, TrustManagerFactory, SSLContext}

import com.typesafe.config.Config
import spray.io.ServerSSLEngineProvider

/**
  * Created by pbanavara on 08/02/16.
  */
trait EmilySSLConfig {
  def sslConfig: Config

  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
  // since we want non-default settings in this example we make a custom SSLContext available here
  implicit def sslContext: SSLContext = {
    val keyStoreResource = sslConfig.getString("certificate-file")
    val password = sslConfig.getString("certificate-password")

    val keyStore = KeyStore.getInstance("JKS")
    val in = getClass.getClassLoader.getResourceAsStream(keyStoreResource)
    require(in != null, "Bad java key storage file: " + keyStoreResource)
    keyStore.load(in, password.toCharArray)

    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  // if there is no ServerSSLEngineProvider in scope implicitly the HttpServer uses the default one,
  // since we want to explicitly enable cipher suites and protocols we make a custom ServerSSLEngineProvider
  // available here
  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
      engine
    }
  }

}
