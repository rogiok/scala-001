package riak

import org.junit.{Ignore, Test}
import com.basho.riak.client.RiakFactory
import com.basho.riak.client.raw.http.{HTTPClusterConfig, HTTPClientConfig}
import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.HttpHost
import org.apache.http.conn.routing.HttpRoute
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.{HttpGet, HttpPut}
import org.apache.http.entity.StringEntity
import akka.actor.{Props, ActorSystem, Actor}
import java.util
import org.apache.http.util.EntityUtils
import com.fasterxml.jackson.core.`type`.TypeReference
import java.util.concurrent.{Executors, ExecutorService}
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Date

/**
 * User: Rogerio
 * Date: 5/19/13
 * Time: 4:17 PM
 */
@Ignore
class RiakStressTest {

  @Test def testOne() {

    val conf = new HTTPClientConfig.Builder()
      .withHost("127.0.0.1")
      .withPort(8098)
      .build()

    val clusterConf = new HTTPClusterConfig(200)
    clusterConf.addClient(conf)

    // create a client (see Configuration below in this README for more details)
    val riakClient = RiakFactory.newClient(clusterConf)

    // create a new bucket
    val myBucket = riakClient.createBucket("Special").nVal(1).execute()

    for (i <- (0 until 100000).par) {
      // add data to the bucket
      myBucket.store("123", "{\"value\": \"Today is \u1234 special day - Éê\"}").w(1).execute()
    }

    //fetch it back
//    IRiakObject myData = myBucket.fetch("key1").execute();

    // you can specify extra parameters to the store operation using the
    // fluent builder style API
//    myData = myBucket.store("key1", "value2").returnBody(true).execute();

    // delete
//    myBucket.delete("key1").rw(3).execute();

  }

  @Test def testTwo() {

    val schemeRegistry = new SchemeRegistry()
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()))
//    schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

    val cm = new PoolingClientConnectionManager(schemeRegistry)
    // Increase max total connection to 200
    cm.setMaxTotal(200)
    // Increase default max connection per route to 20
    cm.setDefaultMaxPerRoute(20)
    // Increase max connections for localhost:80 to 50
    val localhost = new HttpHost("locahost", 80)
    cm.setMaxPerRoute(new HttpRoute(localhost), 50)

    val httpClient = new DefaultHttpClient(cm)

    var riakVclock: String = ""

    for (i <- (0 until 100000).par) {

      val get = new HttpGet(s"http://localhost:8098/buckets/Special/keys/456?r=1")

      get.setHeader("Accept", "application/json")

      var response = httpClient.execute(get)

      if (response.getStatusLine.getStatusCode == 200)
        riakVclock = response.getFirstHeader("X-Riak-Vclock").getValue

      get.releaseConnection()

      val post = new HttpPut(s"http://localhost:8098/buckets/Special/keys/456?w=1")

      post.setEntity(new StringEntity("{\"value\": \"Today is \u1234 special day - Éê\"}", "UTF-8"))
      post.setHeader("Content-Type", "application/json; charset=UTF-8")
      if (riakVclock != "")
        post.setHeader("X-Riak-Vclock", riakVclock)

      response = httpClient.execute(post)

      if (response.getStatusLine.getStatusCode != 204)
        throw new Exception("Error HTTP")

      post.releaseConnection()
    }

//    println(response.getStatusLine.getStatusCode)

  }

  @Test def testThree() {

    val service: ExecutorService = Executors.newFixedThreadPool(10)
    val system = ActorSystem("MySystem")

    val twoActor = system.actorOf(Props(new RiakActor()), name = "riakActor")

    for (i <- (1 to 1000).par) {
      service.execute(new Runnable() {
        def run() {
          println("Sent to " + i)

          twoActor ! "Hello world!!! " + i
        }
      })
    }

    Thread.sleep(20000)

    system.shutdown

  }

}

class RiakActor extends Actor {

  private val mapper: ObjectMapper = new ObjectMapper()
  private var httpClient: DefaultHttpClient = _

  override def preStart() = {
    println("PreStart")

    init()
  }

  def init() {

    val schemeRegistry = new SchemeRegistry()
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()))

    val cm = new PoolingClientConnectionManager(schemeRegistry)
    // Increase max total connection to 200
    cm.setMaxTotal(200)
    // Increase default max connection per route to 20
    cm.setDefaultMaxPerRoute(20)
    // Increase max connections for localhost:80 to 50
    val localhost = new HttpHost("locahost", 80)
    cm.setMaxPerRoute(new HttpRoute(localhost), 50)

    httpClient = new DefaultHttpClient(cm)

  }

  def receive = {
    case id: String => {

      var riakVclock: String = ""
      var list = new util.ArrayList[String]()

      val get = new HttpGet(s"http://localhost:8098/buckets/IdList/keys/Unique?r=1")

      get.setHeader("Accept", "application/json")

      var response = httpClient.execute(get)

      if (response.getStatusLine.getStatusCode == 200) {
        riakVclock = response.getFirstHeader("X-Riak-Vclock").getValue

        list = mapper.readValue[util.ArrayList[String]](
          EntityUtils.toString(response.getEntity, "UTF-8"), new TypeReference[util.ArrayList[String]]() {}
        )
      }

      get.releaseConnection()


      list.add(id)


      val post = new HttpPut(s"http://localhost:8098/buckets/IdList/keys/Unique?w=1")

      post.setEntity(new StringEntity(mapper.writeValueAsString(list), "UTF-8"))
      post.setHeader("Content-Type", "application/json; charset=UTF-8")
      if (riakVclock != "")
        post.setHeader("X-Riak-Vclock", riakVclock)

      response = httpClient.execute(post)

      if (response.getStatusLine.getStatusCode != 204)
        throw new Exception("Error HTTP")

      post.releaseConnection()

      println(s"${new Date} $id sent")
    }
  }
}

