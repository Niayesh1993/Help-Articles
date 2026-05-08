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

        private fun articleDetailJson(id: String): String = ARTICLE_DETAILS_JSON[id] ?: unknownArticleDetailJson(id)

        private val ARTICLE_DETAILS_JSON = mapOf(
            "1" to """
                {
                  "article": {
                    "id": "1",
                    "title": "Getting Started with Help Articles",
                    "content": "## Welcome to the Help Center\n\nUse help articles to quickly find answers, learn workflows, and troubleshoot common questions.\n\n## Find an article\n\nStart from the article list and use the search field to filter by title, summary, or category.\n\n## Read details\n\nTap any article card to open the full guide. Detail pages may include headings, lists, tips, and step-by-step instructions.\n\n## Helpful habits\n\n- Search with short keywords\n- Check the category chip\n- Review the updated date\n\n> Tip: If you are offline, previously cached articles may still be available.",
                    "updatedAt": 1704067200000,
                    "category": "Onboarding"
                  }
                }
            """.trimIndent(),
            "2" to """
                {
                  "article": {
                    "id": "2",
                    "title": "Account Settings and Security",
                    "content": "## Keep your account secure\n\nYour account settings let you manage profile information, password changes, and security preferences.\n\n## Update your profile\n\nOpen **Account Settings** and review your name, email address, and notification preferences.\n\n## Change your password\n\nChoose a strong password that you do not reuse on other services. After saving, sign in again on your trusted devices.\n\n## Enable extra protection\n\nTurn on two-factor authentication when available. This adds another verification step during sign in.\n\n## Security checklist\n\n- Use a unique password\n- Keep your recovery email current\n- Review active sessions regularly",
                    "updatedAt": 1706745600000,
                    "category": "Account"
                  }
                }
            """.trimIndent(),
            "3" to """
                {
                  "article": {
                    "id": "3",
                    "title": "Billing and Subscriptions",
                    "content": "## Manage billing with confidence\n\nBilling settings help you understand invoices, payment methods, subscription plans, and renewal dates.\n\n## Review invoices\n\nOpen the billing page and select an invoice to see charges, taxes, discounts, and payment status.\n\n## Update payment method\n\nAdd a new card or payment account before your renewal date to avoid service interruption.\n\n## Change your plan\n\nWhen upgrading or downgrading, review the summary before confirming so you understand any prorated charges.\n\n> Note: Some subscription changes may take effect at the next billing cycle.",
                    "updatedAt": 1709251200000,
                    "category": "Billing"
                  }
                }
            """.trimIndent(),
            "4" to """
                {
                  "article": {
                    "id": "4",
                    "title": "Troubleshooting Common Issues",
                    "content": "## Start with quick checks\n\nMost issues can be solved with a few basic troubleshooting steps.\n\n## Refresh your data\n\nPull to refresh or tap the refresh button to request the latest articles from the server.\n\n## Check connectivity\n\nIf content does not load, verify your internet connection and try again. Cached content may appear while offline.\n\n## Clear temporary state\n\nClose and reopen the app if the UI looks stuck or outdated.\n\n## Contact support\n\nIf the problem continues, include the error message, time of occurrence, and the action you were trying to complete.",
                    "updatedAt": 1711929600000,
                    "category": "Support"
                  }
                }
            """.trimIndent(),
            "5" to """
                {
                  "article": {
                    "id": "5",
                    "title": "API Reference and Integrations",
                    "content": "## Build with integrations\n\nUse the API to connect help content with internal tools, dashboards, or customer support workflows.\n\n## Authentication\n\nGenerate an API token from developer settings and send it with each request using an authorization header.\n\n## Common endpoints\n\n- `GET /articles` returns article summaries\n- `GET /articles/{id}` returns full article content\n- `GET /articles/error` simulates a backend error payload\n\n## Best practices\n\nCache responses when possible, handle network failures gracefully, and surface backend error messages clearly to users.",
                    "updatedAt": 1714521600000,
                    "category": "Developer"
                  }
                }
            """.trimIndent()
        )

        private fun unknownArticleDetailJson(id: String) = """
            {
              "article": {
                "id": "$id",
                "title": "Article $id — Detailed Guide",
                "content": "## Introduction\n\nWelcome to article **$id**. This fallback guide is used when a mock detail article is not explicitly defined.\n\n## Summary\n\nAdd this ID to the mock detail map if you want custom demo content.",
                "updatedAt": 1714521600000,
                "category": "Guide"
              }
            }
        """.trimIndent()
    }
}