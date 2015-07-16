package jacky.test.httpsniffertest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import org.littleshoot.proxy.ActivityTracker;
import org.littleshoot.proxy.FlowContext;
import org.littleshoot.proxy.FullFlowContext;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLSession;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Created by jacky on 15/7/15.
 */
public class HttpSnifferApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8080");

        setWebViewProxy("127.0.0.1",8080);

        final String tag = "localproxy";
        HttpProxyServer server =
                DefaultHttpProxyServer.bootstrap()
                        .withPort(8080)
                        .plusActivityTracker(new ActivityTracker() {
                            @Override
                            public void clientConnected(InetSocketAddress clientAddress) {
                                Log.d(tag,"clientConnected,"+clientAddress);
                            }

                            @Override
                            public void clientSSLHandshakeSucceeded(InetSocketAddress clientAddress, SSLSession sslSession) {

                            }

                            @Override
                            public void clientDisconnected(InetSocketAddress clientAddress, SSLSession sslSession) {

                            }

                            @Override
                            public void bytesReceivedFromClient(FlowContext flowContext, int numberOfBytes) {

                            }

                            @Override
                            public void requestReceivedFromClient(FlowContext flowContext, HttpRequest httpRequest) {
                                Log.d(tag,"requestReceivedFromClient,"+httpRequest.getUri());
                            }

                            @Override
                            public void bytesSentToServer(FullFlowContext flowContext, int numberOfBytes) {

                            }

                            @Override
                            public void requestSentToServer(FullFlowContext flowContext, HttpRequest httpRequest) {

                            }

                            @Override
                            public void bytesReceivedFromServer(FullFlowContext flowContext, int numberOfBytes) {

                            }

                            @Override
                            public void responseReceivedFromServer(FullFlowContext flowContext, HttpResponse httpResponse) {
                                Log.d(tag,"responseReceivedFromServer,"+httpResponse.getStatus());
                            }

                            @Override
                            public void bytesSentToClient(FlowContext flowContext, int numberOfBytes) {

                            }

                            @Override
                            public void responseSentToClient(FlowContext flowContext, HttpResponse httpResponse) {

                            }
                        })
                        .start();
    }

    private void setWebViewProxy(String host,int port){
        try {
            Class applictionCls = Class.forName("android.app.Application");
            Field loadedApkField = applictionCls.getDeclaredField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(this);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        /*********** optional, may be need in future *************/
                        final String CLASS_NAME = "android.net.ProxyProperties";
                        Class cls = Class.forName(CLASS_NAME);
                        Constructor constructor = cls.getConstructor(String.class, Integer.TYPE, String.class);
                        constructor.setAccessible(true);
                        Object proxyProperties = constructor.newInstance(host, port, null);
                        intent.putExtra("proxy", (Parcelable) proxyProperties);
                        /*********** optional, may be need in future *************/

                        onReceiveMethod.invoke(rec, this, intent);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
