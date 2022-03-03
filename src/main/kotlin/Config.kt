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

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
enum class Reaction{
    Mute, Kick, Admin, NameCard
}
enum class Trigger{
    At, Nudge, Both
}
object Config: AutoSavePluginConfig("config") {
    @ValueDescription("触发条件, 现在支持:At/Nudge = 戳一戳/Both = At + nudge")
    val triggerType: Trigger by value(Trigger.At)

    @ValueDescription("附加字符, 比如是 {xxx, xx}, 那`@11 xxx`和`@11 xx`就会触发, nudge情况下无效")
    val triggers: List<String> by value(listOf("击剑"))

    @ValueDescription("反应, Mute = 禁言/ Kick = 踢出/ Admin = 提升成管理员 / NameCard = 更改成名片列表中随机一个")
    val reaction: Reaction by value(Reaction.Mute)

    @ValueDescription("禁言时间, 当reaction == 1时生效, 单位ms")
    val muteTime by value(10)

    @ValueDescription("结束后发送, {winat} 和 {loseat}代表结果")
    val respond by value("{winat}aa")

    @ValueDescription("随机群名片列表")
    val nameCards by value(listOf("a", "b"))
}