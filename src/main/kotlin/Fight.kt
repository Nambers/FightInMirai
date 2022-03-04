/*
 * Copyright (c) 2022 - 2022. Eritque arcus and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version(in your opinion).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package tech.eritquearcus.fight

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import kotlin.random.Random

private fun getRespond(m1: NormalMember, m2: NormalMember, success: Boolean): String {
    val w = if (success) m1 else m2 // winners
    return (if (m2.id == m2.bot.id) Config.respondWhenTargetIsBot
    else when (Config.respondType) {
        RespondType.Normal -> Config.respond
        RespondType.SuccessOrFail -> if (success) Config.successResponds else Config.failResponds
    })
        .random()
        .replace("{winat}", w.at().serializeToMiraiCode())
        .replace("{loseat}", (if (w == m2) m1 else m2).at().serializeToMiraiCode())
        .replace("{usrat}", w.at().serializeToMiraiCode())
}

// m1 sender, m2 at subject
private suspend fun react(m1: NormalMember, m2: NormalMember) =
    (if (m2.id == m2.bot.id) m1 else (if (Random.nextBoolean()) m1 else m2)).apply {
        if (this.id == bot.id) {
            Fight.logger.error("Bot can not operate itself")
            return@apply
        }
        when (Config.reaction) {
            Reaction.Mute -> this.mute(Config.muteTime)
            Reaction.Kick -> this.kick(Config.kickMessages.random())
            Reaction.Admin -> this.modifyAdmin(true)
            Reaction.NameCard -> this.nameCard = Config.nameCards.random()
        }
    }.let {
        MiraiCode.deserializeMiraiCode(getRespond(m1, m2, it == m1))
    }

object Fight : KotlinPlugin(
    JvmPluginDescription(
        id = "tech.eritquearcus.fight",
        version = "1.0.2",
    )
) {
    override fun onEnable() {
        Config.reload()
        logger.info { "config path:$configFolder\\config.json" }
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            if (this.group.botAsMember.permission < MemberPermission.ADMINISTRATOR) {
                logger.error("bot没有权限, group(${group.id})")
                return@subscribeAlways
            }
            if ((Config.triggerType == Trigger.At || Config.triggerType == Trigger.Both) && message[1] is At && message.size == 3 && message[2] is PlainText && message[2].content.trim() in Config.triggers) {
                val target = this.group[(message[1] as At).target]
                if (target == null) logger.error("找不到目标群成员")
                this.group.sendMessage(react(sender as NormalMember, target!!))
            }
        }
        if (Config.triggerType >= Trigger.Nudge) {
            globalEventChannel().subscribeAlways<NudgeEvent> {
                if (this.subject is Group) {
                    val g = this.subject as Group
                    if (g.botAsMember.permission < MemberPermission.ADMINISTRATOR) {
                        logger.error("bot没有权限, group(${(this.subject as Group).id})")
                    }
                    this.subject.sendMessage(react(g[this.from.id]!!, g[this.target.id]!!))
                }
            }
        }
    }
}