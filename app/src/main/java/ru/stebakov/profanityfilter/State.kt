package ru.stebakov.profanityfilter

import java.util.Collections
import java.util.TreeMap
import java.util.TreeSet


class State
/**
 * 构造深度为0的节点
 */ @JvmOverloads constructor(
    /**
     * 模式串的长度，也是这个状态的深度
     */
    val depth: Int = 0
) {
    /**
     * 获取节点深度
     *
     * @return
     */

    /**
     * fail 函数，如果没有匹配到，则跳转到此状态。
     */
    private var failure: State? = null

    /**
     * 只要这个状态可达，则记录模式串
     */
    private var emits: MutableSet<Int>? = null

    /**
     * goto 表，也称转移函数。根据字符串的下一个字符转移到下一个状态
     */
    private val success: MutableMap<Char, State> = TreeMap()

    /**
     * 在双数组中的对应下标
     */
    var index = 0
    /**
     * 构造深度为depth的节点
     *
     * @param depth
     */

    /**
     * 添加一个匹配到的模式串（这个状态对应着这个模式串)
     *
     * @param keyword
     */
    fun addEmit(keyword: Int) {
        if (emits == null) {
            emits = TreeSet(Collections.reverseOrder())
        }
        emits!!.add(keyword)
    }

    /**
     * 获取最大的值
     *
     * @return
     */
    val largestValueId: Int?
        get() = if (emits == null || emits!!.size == 0) null else emits!!.iterator().next()

    /**
     * 添加一些匹配到的模式串
     *
     * @param emits
     */
    fun addEmit(emits: Collection<Int>) {
        for (emit in emits) {
            addEmit(emit)
        }
    }

    /**
     * 获取这个节点代表的模式串（们）
     *
     * @return
     */
    fun emit(): Collection<Int> {
        return if (emits == null) emptyList() else emits!!
    }

    /**
     * 是否是终止状态
     *
     * @return
     */
    val isAcceptable: Boolean
        get() = depth > 0 && emits != null

    /**
     * 获取failure状态
     *
     * @return
     */
    fun failure(): State? {
        return failure
    }

    /**
     * 设置failure状态
     *
     * @param failState
     */
    fun setFailure(failState: State, fail: IntArray) {
        failure = failState
        fail[index] = failState.index
    }

    /**
     * 转移到下一个状态
     *
     * @param character       希望按此字符转移
     * @param ignoreRootState 是否忽略根节点，如果是根节点自己调用则应该是true，否则为false
     * @return 转移结果
     */
    private fun nextState(character: Char, ignoreRootState: Boolean): State? {
        var nextState = success[character]
        if (!ignoreRootState && nextState == null && depth == 0) {
            nextState = this
        }
        return nextState
    }

    /**
     * 按照character转移，根节点转移失败会返回自己（永远不会返回null）
     *
     * @param character
     * @return
     */
    fun nextState(character: Char): State? {
        return nextState(character, false)
    }

    /**
     * 按照character转移，任何节点转移失败会返回null
     *
     * @param character
     * @return
     */
    fun nextStateIgnoreRootState(character: Char): State? {
        return nextState(character, true)
    }

    fun addState(character: Char): State {
        var nextState = nextStateIgnoreRootState(character)
        if (nextState == null) {
            nextState = State(depth + 1)
            success[character] = nextState
        }
        return nextState
    }

    val states: Collection<State>
        get() = success.values
    val transitions: Collection<Char>
        get() = success.keys

    override fun toString(): String {
        val sb = StringBuilder("State{")
        sb.append("depth=").append(depth)
        sb.append(", ID=").append(index)
        sb.append(", emits=").append(emits)
        sb.append(", success=").append(success.keys)
        sb.append(", failureID=").append(if (failure == null) "-1" else failure!!.index)
        sb.append(", failure=").append(failure)
        sb.append('}')
        return sb.toString()
    }

    /**
     * 获取goto表
     *
     * @return
     */
    fun getSuccess(): Map<Char, State> {
        return success
    }
}
