package ie.app.freelanchaincode

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback


class ElasticsearchService {
    companion object {
        val client: ElasticsearchClient = getElasticsearchClient()

        private fun getElasticsearchClient(): ElasticsearchClient {
            try {

                val builder = RestClient.builder(HttpHost("10.0.2.2", 9200))

                val httpClientConfigCallback =
                    HttpClientConfigCallback { httpClientBuilder ->
                        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
                        val authScope = AuthScope("10.0.2.2", 9200)
                        val usernamePasswordCredentials = UsernamePasswordCredentials(
                            "elastic",
                            "3hY_xzv34NS2s14iCB8t"
                        )
                        credentialsProvider.setCredentials(authScope, usernamePasswordCredentials)
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                        httpClientBuilder
                    }

                builder.setHttpClientConfigCallback(httpClientConfigCallback)

                val restClient = builder.build()

                val transport: ElasticsearchTransport =
                    RestClientTransport(restClient, JacksonJsonpMapper())

            return ElasticsearchClient(transport)
            } catch (e: Exception) {
                println("Failed to initialize Elasticsearch client: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

}