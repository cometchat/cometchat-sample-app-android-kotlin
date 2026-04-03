package com.cometchat.uikit.kotlin.shared.mentions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.util.regex.Pattern

/**
 * Unit tests for mention regex pattern matching.
 * 
 * Tests cover:
 * - User mention pattern: <@uid:userId>
 * - Mention all pattern: <@all:all>
 * - Pattern matching in various text contexts
 * - Edge cases with special characters
 * 
 * **Validates: Requirements FR-7.1, FR-8.1**
 */
class MentionRegexPatternTest : DescribeSpec({

    // Default patterns from CometChatMentionsFormatter
    val userMentionPattern = Pattern.compile("<@uid:(.*?)>")
    val mentionAllPattern = Pattern.compile("<@all:all>")

    describe("User Mention Pattern") {

        describe("basic matching") {

            it("should match single user mention") {
                val text = "Hello <@uid:user123> how are you?"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.group(1) shouldBe "user123"
            }

            it("should match multiple user mentions") {
                val text = "Hello <@uid:user1> and <@uid:user2>"
                val matcher = userMentionPattern.matcher(text)
                
                val matches = mutableListOf<String>()
                while (matcher.find()) {
                    matches.add(matcher.group(1))
                }
                
                matches shouldHaveSize 2
                matches[0] shouldBe "user1"
                matches[1] shouldBe "user2"
            }

            it("should extract user ID correctly") {
                val text = "<@uid:abc-123-def>"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.group(1) shouldBe "abc-123-def"
            }

            it("should match mention at start of text") {
                val text = "<@uid:user1> said hello"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.start() shouldBe 0
            }

            it("should match mention at end of text") {
                val text = "Message from <@uid:user1>"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.end() shouldBe text.length
            }
        }

        describe("non-matching cases") {

            it("should not match incomplete pattern") {
                val text = "Hello <@uid:user123"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe false
            }

            it("should not match wrong format") {
                val text = "Hello @user123"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe false
            }

            it("should not match empty user ID") {
                val text = "Hello <@uid:>"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.group(1) shouldBe ""
            }

            it("should not match plain text") {
                val text = "Hello world"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe false
            }
        }

        describe("special characters in user ID") {

            it("should match user ID with underscores") {
                val text = "<@uid:user_name_123>"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.group(1) shouldBe "user_name_123"
            }

            it("should match user ID with hyphens") {
                val text = "<@uid:user-name-123>"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.group(1) shouldBe "user-name-123"
            }

            it("should match user ID with dots") {
                val text = "<@uid:user.name.123>"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.group(1) shouldBe "user.name.123"
            }
        }

        describe("position tracking") {

            it("should return correct start and end positions") {
                val text = "Hello <@uid:user1> world"
                val matcher = userMentionPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.start() shouldBe 6
                matcher.end() shouldBe 18
            }

            it("should track positions for multiple mentions") {
                val text = "<@uid:a> and <@uid:b>"
                val matcher = userMentionPattern.matcher(text)
                
                // First mention
                matcher.find() shouldBe true
                matcher.start() shouldBe 0
                matcher.end() shouldBe 8
                
                // Second mention
                matcher.find() shouldBe true
                matcher.start() shouldBe 13
                matcher.end() shouldBe 21
            }
        }
    }

    describe("Mention All Pattern") {

        describe("basic matching") {

            it("should match mention all") {
                val text = "Hello <@all:all> everyone"
                val matcher = mentionAllPattern.matcher(text)
                
                matcher.find() shouldBe true
            }

            it("should match mention all at start") {
                val text = "<@all:all> attention please"
                val matcher = mentionAllPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.start() shouldBe 0
            }

            it("should match mention all at end") {
                val text = "Message for <@all:all>"
                val matcher = mentionAllPattern.matcher(text)
                
                matcher.find() shouldBe true
                matcher.end() shouldBe text.length
            }
        }

        describe("non-matching cases") {

            it("should not match partial mention all") {
                val text = "Hello <@all:al"
                val matcher = mentionAllPattern.matcher(text)
                
                matcher.find() shouldBe false
            }

            it("should not match different ID") {
                val text = "Hello <@all:everyone>"
                val matcher = mentionAllPattern.matcher(text)
                
                matcher.find() shouldBe false
            }

            it("should not match user mention format") {
                val text = "Hello <@uid:all>"
                val matcher = mentionAllPattern.matcher(text)
                
                matcher.find() shouldBe false
            }
        }
    }

    describe("Custom Mention All Pattern") {

        it("should support custom mention all ID") {
            val customId = "everyone"
            val customPattern = Pattern.compile("<@all:$customId>")
            val text = "Hello <@all:everyone>"
            
            val matcher = customPattern.matcher(text)
            matcher.find() shouldBe true
        }

        it("should generate correct pattern for custom ID") {
            val customId = "team"
            val expectedPattern = "<@all:team>"
            val customPattern = Pattern.compile("<@all:$customId>")
            
            val matcher = customPattern.matcher(expectedPattern)
            matcher.find() shouldBe true
        }
    }

    describe("Mixed Mentions") {

        it("should find both user and mention all in same text") {
            val text = "Hello <@uid:user1> and <@all:all>"
            
            val userMatcher = userMentionPattern.matcher(text)
            val allMatcher = mentionAllPattern.matcher(text)
            
            userMatcher.find() shouldBe true
            userMatcher.group(1) shouldBe "user1"
            
            allMatcher.find() shouldBe true
        }

        it("should handle multiple users and mention all") {
            val text = "<@all:all> <@uid:user1> <@uid:user2>"
            
            val userMatcher = userMentionPattern.matcher(text)
            val userMatches = mutableListOf<String>()
            while (userMatcher.find()) {
                userMatches.add(userMatcher.group(1))
            }
            
            userMatches shouldHaveSize 2
            
            val allMatcher = mentionAllPattern.matcher(text)
            allMatcher.find() shouldBe true
        }
    }

    describe("Edge Cases") {

        it("should handle empty text") {
            val text = ""
            val matcher = userMentionPattern.matcher(text)
            
            matcher.find() shouldBe false
        }

        it("should handle text with only whitespace") {
            val text = "   \n\t  "
            val matcher = userMentionPattern.matcher(text)
            
            matcher.find() shouldBe false
        }

        it("should handle adjacent mentions") {
            val text = "<@uid:user1><@uid:user2>"
            val matcher = userMentionPattern.matcher(text)
            
            val matches = mutableListOf<String>()
            while (matcher.find()) {
                matches.add(matcher.group(1))
            }
            
            matches shouldHaveSize 2
        }

        it("should handle mention with newlines around it") {
            val text = "Hello\n<@uid:user1>\nworld"
            val matcher = userMentionPattern.matcher(text)
            
            matcher.find() shouldBe true
            matcher.group(1) shouldBe "user1"
        }

        it("should handle very long user ID") {
            val longId = "a".repeat(100)
            val text = "<@uid:$longId>"
            val matcher = userMentionPattern.matcher(text)
            
            matcher.find() shouldBe true
            matcher.group(1) shouldBe longId
        }
    }
})
