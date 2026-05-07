package com.zozi.helparticlesapp.data.mock

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * OkHttp interceptor that short-circuits all requests and returns fake responses.
 *
 * Scenarios covered:
 *   - GET /articles          → normal list response
 *   - GET /articles/{id}     → normal detail response for IDs 1-5
 *   - GET /articles/error    → backend-provided error payload (HTTP 200 + error body)
 *   - ~20% of requests       → random transport-level error (IOException / 500)
 */
@Singleton
class MockInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.encodedPath

        // Simulate occasional transport errors on ~20% of requests
        if (Random.Default.nextFloat() < TRANSPORT_ERROR_RATE) {
            return when (Random.Default.nextInt(2)) {
                0 -> throw IOException("Simulated network timeout")
                else -> buildResponse(chain.request(), 500, """{"error":"Internal server error"}""")
            }
        }

        return when {
            url == "/articles" -> buildResponse(chain.request(), 200, ARTICLE_LIST_JSON)
            url == "/articles/error" -> buildResponse(chain.request(), 200, BACKEND_ERROR_JSON)
            url.startsWith("/articles/") -> {
                val id = url.removePrefix("/articles/")
                buildResponse(chain.request(), 200, articleDetailJson(id))
            }
            else -> buildResponse(chain.request(), 404, """{"error":"Not found"}""")
        }
    }

    private fun buildResponse(request: Request, code: Int, body: String): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()

    companion object {
        private const val TRANSPORT_ERROR_RATE = 0.2f

        val ARTICLE_LIST_JSON = """
            {
              "articles": [
                {
                  "id": "1",
                  "title": "Getting Started with Help Articles",
                  "summary": "Learn how to navigate our help center and find answers quickly.",
                  "updatedAt": 1704067200000,
                  "category": "Onboarding"
                },
                {
                  "id": "2",
                  "title": "Account Settings and Security",
                  "summary": "Manage your profile, change passwords, and enable two-factor authentication.",
                  "updatedAt": 1706745600000,
                  "category": "Account"
                },
                {
                  "id": "3",
                  "title": "Billing and Subscriptions",
                  "summary": "Understand your invoice, update payment methods, and manage plans.",
                  "updatedAt": 1709251200000,
                  "category": "Billing"
                },
                {
                  "id": "4",
                  "title": "Troubleshooting Common Issues",
                  "summary": "Step-by-step solutions for the most frequently reported problems.",
                  "updatedAt": 1711929600000,
                  "category": "Support"
                },
                {
                  "id": "5",
                  "title": "API Reference and Integrations",
                  "summary": "Connect third-party tools and explore our developer API.",
                  "updatedAt": 1714521600000,
                  "category": "Developer"
                },
                {
                  "id": "error",
                  "title": "Article That Returns a Backend Error",
                  "summary": "Tap to simulate a backend-provided error response.",
                  "updatedAt": 1714521600000,
                  "category": "Demo"
                }
              ]
            }
        """.trimIndent()

        private val BACKEND_ERROR_JSON = """
            {
              "error": {
                "errorCode": "ARTICLE_UNAVAILABLE",
                "errorTitle": "Content Unavailable",
                "errorMessage": "This article has been temporarily removed for review. Please check back later."
              }
            }
        """.trimIndent()

        private fun articleDetailJson(id: String) = """
            {
              "article": {
                "id": "$id",
                "title": "Article $id — Detailed Guide",
                "content": "## Introduction\n\nWelcome to article **$id**. This guide covers everything you need to know.\n\n## Step 1\n\nStart by opening the settings panel from the top-right menu.\n\n## Step 2\n\nNavigate to **Account > Security** and enable two-factor authentication.\n\n## Step 3\n\nVerify your changes by logging out and back in.\n\n> **Tip:** You can always reset your preferences from the account page.\n\n## Summary\n\nFollowing these steps ensures your account remains secure.",
                "updatedAt": 1714521600000,
                "category": "Guide"
              }
            }
        """.trimIndent()
    }
}