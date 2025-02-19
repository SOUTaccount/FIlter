package ru.stebakov.profanityfilter

import com.hankcs.algorithm.State
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.AbstractMap
import java.util.ArrayDeque
import java.util.Arrays
import java.util.Queue

class AhoCorasickDoubleArrayTrie<V> : Serializable {

    /**
     * check array of the Double Array Trie structure
     */
    protected var check: IntArray = intArrayOf()


    /**
     * base array of the Double Array Trie structure
     */
    protected var base: IntArray= intArrayOf()


    /**
     * fail table of the Aho Corasick automata
     */
    protected var fail: IntArray= intArrayOf()

    /**
     * output table of the Aho Corasick automata
     */
    protected var output: Array<IntArray?> = arrayOf(intArrayOf())

    /**
     * outer value array
     */
    lateinit var v: Array<V>

    /**
     * the length of every key
     */
    var l: IntArray = intArrayOf()

    /**
     * the size of base and check array
     */
    protected var size = 0

    /**
     * Parse text
     *
     * @param text The text
     * @return a list of outputs
     */
    fun parseText(text: CharSequence): List<Hit<V>> {
        var position = 1
        var currentState = 0
        val collectedEmits: MutableList<Hit<V>> = ArrayList()
        for (i in 0 until text.length) {
            currentState = getState(currentState, text[i])
            storeEmits(position, currentState, collectedEmits)
            ++position
        }
        return collectedEmits
    }

    /**
     * Parse text
     *
     * @param text      The text
     * @param processor A processor which handles the output
     */
    fun parseText(text: CharSequence, processor: IHit<V>) {
        var position = 1
        var currentState = 0
        for (i in 0 until text.length) {
            currentState = getState(currentState, text[i])
            val hitArray = output[currentState]
            if (hitArray != null) {
                for (hit in hitArray) {
                    processor.hit(position - l[hit], position, v[hit])
                }
            }
            ++position
        }
    }

    /**
     * Parse text
     *
     * @param text      The text
     * @param processor A processor which handles the output
     */
    fun parseText(text: CharSequence, processor: IHitCancellable<V>) {
        var currentState = 0
        for (i in 0 until text.length) {
            val position = i + 1
            currentState = getState(currentState, text[i])
            val hitArray = output[currentState]
            if (hitArray != null) {
                for (hit in hitArray) {
                    val proceed = processor.hit(position - l[hit], position, v[hit])
                    if (!proceed) {
                        return
                    }
                }
            }
        }
    }

    /**
     * Parse text
     *
     * @param text      The text
     * @param processor A processor which handles the output
     */
    fun parseText(text: CharArray, processor: IHit<V>) {
        var position = 1
        var currentState = 0
        for (c in text) {
            currentState = getState(currentState, c)
            val hitArray = output[currentState]
            if (hitArray != null) {
                for (hit in hitArray) {
                    processor.hit(position - l[hit], position, v[hit])
                }
            }
            ++position
        }
    }

    /**
     * Parse text
     *
     * @param text      The text
     * @param processor A processor which handles the output
     */
    fun parseText(text: CharArray, processor: IHitFull<V>) {
        var position = 1
        var currentState = 0
        for (c in text) {
            currentState = getState(currentState, c)
            val hitArray = output[currentState]
            if (hitArray != null) {
                for (hit in hitArray) {
                    processor.hit(position - l[hit], position, v[hit], hit)
                }
            }
            ++position
        }
    }

    /**
     * Checks that string contains at least one substring
     *
     * @param text source text to check
     * @return `true` if string contains at least one substring
     */
    fun matches(text: String): Boolean {
        var currentState = 0
        for (i in 0 until text.length) {
            currentState = getState(currentState, text[i])
            val hitArray = output[currentState]
            if (hitArray != null) {
                return true
            }
        }
        return false
    }

    /**
     * Search first match in string
     *
     * @param text source text to check
     * @return first match or `null` if there are no matches
     */
    fun findFirst(text: String): Hit<V>? {
        var position = 1
        var currentState = 0
        for (i in 0 until text.length) {
            currentState = getState(currentState, text[i])
            val hitArray = output[currentState]
            if (hitArray != null) {
                val hitIndex = hitArray[0]
                return Hit(
                    position - l[hitIndex], position,
                    v[hitIndex]
                )
            }
            ++position
        }
        return null
    }

    /**
     * Save
     *
     * @param out An ObjectOutputStream object
     * @throws IOException Some IOException
     */
    @Throws(IOException::class)
    fun save(out: ObjectOutputStream) {
        out.writeObject(base)
        out.writeObject(check)
        out.writeObject(fail)
        out.writeObject(output)
        out.writeObject(l)
        out.writeObject(v)
    }

    /**
     * Load data from [ObjectInputStream]
     *
     * @param in An ObjectInputStream object
     * @throws IOException            If can't read the file from path
     * @throws ClassNotFoundException If the class doesn't exist or matched
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    fun load(`in`: ObjectInputStream) {
        base = `in`.readObject() as IntArray
        check = `in`.readObject() as IntArray
        fail = `in`.readObject() as IntArray
        output = `in`.readObject() as Array<IntArray?>
        l = `in`.readObject() as IntArray
        v = `in`.readObject() as Array<V>
    }

    /**
     * Get value by a String key, just like a map.get() method
     *
     * @param key The key
     * @return value if exist otherwise it return null
     */
    operator fun get(key: String): V? {
        val index = exactMatchSearch(key)
        return if (index >= 0) {
            v[index]
        } else null
    }

    /**
     * Update a value corresponding to a key
     *
     * @param key   the key
     * @param value the value
     * @return successful or not（failure if there is no key）
     */
    operator fun set(key: String, value: V): Boolean {
        val index = exactMatchSearch(key)
        if (index >= 0) {
            v[index] = value
            return true
        }
        return false
    }

    /**
     * Pick the value by index in value array <br></br>
     * Notice that to be more efficiently, this method DO NOT check the parameter
     *
     * @param index The index
     * @return The value
     */
    operator fun get(index: Int): V {
        return v[index]
    }

    /**
     * Processor handles the output when hit a keyword
     */
    interface IHit<V> {
        /**
         * Hit a keyword, you can use some code like text.substring(begin, end) to get the keyword
         *
         * @param begin the beginning index, inclusive.
         * @param end   the ending index, exclusive.
         * @param value the value assigned to the keyword
         */
        fun hit(begin: Int, end: Int, value: V)
    }

    /**
     * Processor handles the output when hit a keyword, with more detail
     */
    interface IHitFull<V> {
        /**
         * Hit a keyword, you can use some code like text.substring(begin, end) to get the keyword
         *
         * @param begin the beginning index, inclusive.
         * @param end   the ending index, exclusive.
         * @param value the value assigned to the keyword
         * @param index the index of the value assigned to the keyword, you can use the integer as a perfect hash value
         */
        fun hit(begin: Int, end: Int, value: V, index: Int)
    }

    /**
     * Callback that allows to cancel the search process.
     */
    interface IHitCancellable<V> {
        /**
         * Hit a keyword, you can use some code like text.substring(begin, end) to get the keyword
         *
         * @param begin the beginning index, inclusive.
         * @param end   the ending index, exclusive.
         * @param value the value assigned to the keyword
         * @return Return true for continuing the search and false for stopping it.
         */
        fun hit(begin: Int, end: Int, value: V): Boolean
    }

    /**
     * A result output
     *
     * @param <V> the value type
    </V> */
    class Hit<V>(
        /**
         * the beginning index, inclusive.
         */
        val begin: Int,
        /**
         * the ending index, exclusive.
         */
        val end: Int,
        /**
         * the value assigned to the keyword
         */
        val value: V
    ) {

        override fun toString(): String {
            return String.format("[%d:%d]=%s", begin, end, value)
        }
    }

    /**
     * transmit state, supports failure function
     *
     * @param currentState
     * @param character
     * @return
     */
    private fun getState(currentState: Int, character: Char): Int {
        var currentState = currentState
        var newCurrentState = transitionWithRoot(currentState, character) // 先按success跳转
        while (newCurrentState == -1) // 跳转失败的话，按failure跳转
        {
            currentState = fail[currentState]
            newCurrentState = transitionWithRoot(currentState, character)
        }
        return newCurrentState
    }

    /**
     * store output
     *
     * @param position
     * @param currentState
     * @param collectedEmits
     */
    private fun storeEmits(position: Int, currentState: Int, collectedEmits: MutableList<Hit<V>>) {
        val hitArray = output[currentState]
        if (hitArray != null) {
            for (hit in hitArray) {
                collectedEmits.add(
                    Hit(
                        position - l[hit], position,
                        v[hit]
                    )
                )
            }
        }
    }

    /**
     * transition of a state
     *
     * @param current
     * @param c
     * @return
     */
    protected fun transition(current: Int, c: Char): Int {
        var b = current
        var p: Int
        p = b + c.code + 1
        b = if (b == check[p]) base[p] else return -1
        p = b
        return p
    }

    /**
     * transition of a state, if the state is root and it failed, then returns the root
     *
     * @param nodePos
     * @param c
     * @return
     */
    protected fun transitionWithRoot(nodePos: Int, c: Char): Int {
        val b = base[nodePos]
        val p: Int
        p = b + c.code + 1
        return if (b != check[p]) {
            if (nodePos == 0) 0 else -1
        } else p
    }

    /**
     * Build a AhoCorasickDoubleArrayTrie from a map
     *
     * @param map a map containing key-value pairs
     */
    inline fun <reified value: V> build(map: Map<String, value>) {
        Builder().build(map)
    }

    /**
     * match exactly by a key
     *
     * @param key the key
     * @return the index of the key, you can use it as a perfect hash function
     */
    fun exactMatchSearch(key: String): Int {
        return exactMatchSearch(key, 0, 0, 0)
    }

    /**
     * match exactly by a key
     *
     * @param key
     * @param pos
     * @param len
     * @param nodePos
     * @return
     */
    private fun exactMatchSearch(key: String, pos: Int, len: Int, nodePos: Int): Int {
        var len = len
        var nodePos = nodePos
        if (len <= 0) len = key.length
        if (nodePos <= 0) nodePos = 0
        val result = -1
        val keyChars = key.toCharArray()
        return getMatched(pos, len, result, keyChars, base[nodePos])
    }

    private fun getMatched(pos: Int, len: Int, result: Int, keyChars: CharArray, b1: Int): Int {
        var result = result
        var b = b1
        var p: Int
        for (i in pos until len) {
            p = b + keyChars[i].code + 1
            b = if (b == check[p]) base[p] else return result
        }
        p = b // transition through '\0' to check if it's the end of a word
        val n = base[p]
        if (b == check[p]) // yes, it is.
        {
            result = -n - 1
        }
        return result
    }

    /**
     * match exactly by a key
     *
     * @param keyChars the char array of the key
     * @param pos      the begin index of char array
     * @param len      the length of the key
     * @param nodePos  the starting position of the node for searching
     * @return the value index of the key, minus indicates null
     */
    private fun exactMatchSearch(keyChars: CharArray, pos: Int, len: Int, nodePos: Int): Int {
        val result = -1
        return getMatched(pos, len, result, keyChars, base[nodePos])
    }
    //    /**
    //     * Just for debug when I wrote it
    //     */
    //    public void debug()
    //    {
    //        System.out.println("base:");
    //        for (int i = 0; i < base.length; i++)
    //        {
    //            if (base[i] < 0)
    //            {
    //                System.out.println(i + " : " + -base[i]);
    //            }
    //        }
    //
    //        System.out.println("output:");
    //        for (int i = 0; i < output.length; i++)
    //        {
    //            if (output[i] != null)
    //            {
    //                System.out.println(i + " : " + Arrays.toString(output[i]));
    //            }
    //        }
    //
    //        System.out.println("fail:");
    //        for (int i = 0; i < fail.length; i++)
    //        {
    //            if (fail[i] != 0)
    //            {
    //                System.out.println(i + " : " + fail[i]);
    //            }
    //        }
    //
    //        System.out.println(this);
    //    }
    //
    //    @Override
    //    public String toString()
    //    {
    //        String infoIndex = "i    = ";
    //        String infoChar = "char = ";
    //        String infoBase = "base = ";
    //        String infoCheck = "check= ";
    //        for (int i = 0; i < Math.min(base.length, 200); ++i)
    //        {
    //            if (base[i] != 0 || check[i] != 0)
    //            {
    //                infoChar += "    " + (i == check[i] ? " ×" : (char) (i - check[i] - 1));
    //                infoIndex += " " + String.format("%5d", i);
    //                infoBase += " " + String.format("%5d", base[i]);
    //                infoCheck += " " + String.format("%5d", check[i]);
    //            }
    //        }
    //        return "DoubleArrayTrie：" +
    //                "\n" + infoChar +
    //                "\n" + infoIndex +
    //                "\n" + infoBase +
    //                "\n" + infoCheck + "\n" +
    ////                "check=" + Arrays.toString(check) +
    ////                ", base=" + Arrays.toString(base) +
    ////                ", used=" + Arrays.toString(used) +
    //                "size=" + size
    ////                ", length=" + Arrays.toString(length) +
    ////                ", value=" + Arrays.toString(value) +
    //                ;
    //    }
    //
    //    /**
    //     * 一个顺序输出变量名与变量值的调试类
    //     */
    //    private static class DebugArray
    //    {
    //        Map<String, String> nameValueMap = new LinkedHashMap<String, String>();
    //
    //        public void add(String name, int value)
    //        {
    //            String valueInMap = nameValueMap.get(name);
    //            if (valueInMap == null)
    //            {
    //                valueInMap = "";
    //            }
    //
    //            valueInMap += " " + String.format("%5d", value);
    //
    //            nameValueMap.put(name, valueInMap);
    //        }
    //
    //        @Override
    //        public String toString()
    //        {
    //            String text = "";
    //            for (Map.Entry<String, String> entry : nameValueMap.entrySet())
    //            {
    //                String name = entry.getKey();
    //                String value = entry.getValue();
    //                text += String.format("%-5s", name) + "= " + value + '\n';
    //            }
    //
    //            return text;
    //        }
    //
    //        public void println()
    //        {
    //            System.out.print(this);
    //        }
    //    }
    /**
     * @return the size of the keywords
     */
    fun size(): Int {
        return v.size
    }

    /**
     * A builder to build the AhoCorasickDoubleArrayTrie
     */
    inner class Builder {
        /**
         * the root state of trie
         */
        var rootState: State? = State()

        /**
         * whether the position has been used
         */
        var used: BooleanArray? = booleanArrayOf()

        /**
         * the allocSize of the dynamic array
         */
        private var allocSize = 0

        /**
         * a parameter controls the memory growth speed of the dynamic array
         */
        private var progress = 0

        /**
         * the next position to check unused memory
         */
        private var nextCheckPos = 0

        /**
         * the size of the key-pair sets
         */
        private var keySize = 0

        /**
         * Build from a map
         *
         * @param map a map containing key-value pairs
         */
        inline fun <reified value: V> build(map: Map<String, value>) {
            // 把值保存下来
            val newMap = map.values.toTypedArray()
            v = newMap as Array<V>
            l = IntArray(v.size)
            val keySet = map.keys
            // 构建二分trie树
            addAllKeyword(keySet)
            // 在二分trie树的基础上构建双数组trie树
            buildDoubleArrayTrie(keySet.size)
            used = null
            // 构建failure表并且合并output表
            constructFailureStates()
            rootState = null
            loseWeight()
        }

        /**
         * fetch siblings of a parent node
         *
         * @param parent   parent node
         * @param siblings parent node's child nodes, i . e . the siblings
         * @return the amount of the siblings
         */
        private fun fetch(parent: State?, siblings: MutableList<Map.Entry<Int, State>>): Int {
            if (parent!!.isAcceptable) {
                val fakeNode = State(-(parent.depth + 1)) // 此节点是parent的子节点，同时具备parent的输出
                fakeNode.addEmit(parent.largestValueId)
                siblings.add(AbstractMap.SimpleEntry(0, fakeNode))
            }
            for (entry in parent.success.entries) {
                siblings.add(
                    AbstractMap.SimpleEntry<Int, State>(
                        entry.key.code + 1, entry.value
                    )
                )
            }
            return siblings.size
        }

        /**
         * add a keyword
         *
         * @param keyword a keyword
         * @param index   the index of the keyword
         */
        private fun addKeyword(keyword: String, index: Int) {
            var currentState = rootState
            for (character in keyword.toCharArray()) {
                currentState = currentState!!.addState(character)
            }
            currentState!!.addEmit(index)
            l[index] = keyword.length
        }

        /**
         * add a collection of keywords
         *
         * @param keywordSet the collection holding keywords
         */
        fun addAllKeyword(keywordSet: Collection<String>) {
            var i = 0
            for (keyword in keywordSet) {
                addKeyword(keyword, i++)
            }
        }

        /**
         * construct failure table
         */
        fun constructFailureStates() {
            fail = IntArray(size + 1)
            output = arrayOfNulls(size + 1)
            val queue: Queue<State> = ArrayDeque()

            // 第一步，将深度为1的节点的failure设为根节点
            for (depthOneState in rootState!!.states) {
                depthOneState.setFailure(rootState, fail)
                queue.add(depthOneState)
                constructOutput(depthOneState)
            }

            // 第二步，为深度 > 1 的节点建立failure表，这是一个bfs
            while (!queue.isEmpty()) {
                val currentState = queue.remove()
                for (transition in currentState.transitions) {
                    val targetState = currentState.nextState(transition)
                    queue.add(targetState)
                    var traceFailureState = currentState.failure()
                    while (traceFailureState.nextState(transition) == null) {
                        traceFailureState = traceFailureState.failure()
                    }
                    val newFailureState = traceFailureState.nextState(transition)
                    targetState.setFailure(newFailureState, fail)
                    targetState.addEmit(newFailureState.emit())
                    constructOutput(targetState)
                }
            }
        }

        /**
         * construct output table
         */
        private fun constructOutput(targetState: State) {
            val emit = targetState.emit()
            if (emit == null || emit.size == 0) return
            val output = IntArray(emit.size)
            val it: Iterator<Int> = emit.iterator()
            for (i in output.indices) {
                output[i] = it.next()
            }
            this@AhoCorasickDoubleArrayTrie.output[targetState.index] = output
        }

        fun buildDoubleArrayTrie(keySize: Int) {
            progress = 0
            this.keySize = keySize
            resize(65536 * 32) // 32个双字节
            base[0] = 1
            nextCheckPos = 0
            val root_node = rootState
            val siblings: MutableList<Map.Entry<Int, State>> = ArrayList(
                root_node!!.success.entries.size
            )
            fetch(root_node, siblings)
            if (siblings.isEmpty()) Arrays.fill(
                check,
                -1
            ) // fill -1 such that no transition is allowed
            else insert(siblings)
        }

        /**
         * allocate the memory of the dynamic array
         *
         * @param newSize of the new array
         * @return the new-allocated-size
         */
        private fun resize(newSize: Int): Int {
            val base2 = IntArray(newSize)
            val check2 = IntArray(newSize)
            val used2 = BooleanArray(newSize)
            if (allocSize > 0) {
                System.arraycopy(base, 0, base2, 0, allocSize)
                System.arraycopy(check, 0, check2, 0, allocSize)
                System.arraycopy(used, 0, used2, 0, allocSize)
            }
            base = base2
            check = check2
            used = used2
            return newSize.also { allocSize = it }
        }

        /**
         * insert the siblings to double array trie
         *
         * @param firstSiblings the initial siblings being inserted
         */
        private fun insert(firstSiblings: List<Map.Entry<Int, State>>) {
            val siblingQueue: Queue<Map.Entry<Int?, List<Map.Entry<Int, State>>>> = ArrayDeque()
            siblingQueue.add(
                AbstractMap.SimpleEntry<Int?, List<Map.Entry<Int, State>>>(
                    null,
                    firstSiblings
                )
            )
            while (siblingQueue.isEmpty() == false) {
                insert(siblingQueue)
            }
        }

        /**
         * insert the siblings to double array trie
         *
         * @param siblingQueue a queue holding all siblings being inserted and the position to insert them
         */
        private fun insert(siblingQueue: Queue<Map.Entry<Int?, List<Map.Entry<Int, State>>>>) {
            val (parentBaseIndex, siblings) = siblingQueue.remove()
            var begin = 0
            var pos = Math.max(siblings[0].key + 1, nextCheckPos) - 1
            var nonzero_num = 0
            var first = 0
            if (allocSize <= pos) resize(pos + 1)
            outer@ // 此循环体的目标是找出满足base[begin + a1...an]  == 0的n个空闲空间,a1...an是siblings中的n个节点
            while (true) {
                pos++
                if (allocSize <= pos) resize(pos + 1)
                if (check[pos] != 0) {
                    nonzero_num++
                    continue
                } else if (first == 0) {
                    nextCheckPos = pos
                    first = 1
                }
                begin = pos - siblings[0].key // 当前位置离第一个兄弟节点的距离
                if (allocSize <= begin + siblings[siblings.size - 1].key) {
                    // progress can be zero // 防止progress产生除零错误
                    val toSize = Math.max(1.05, 1.0 * keySize / (progress + 1)) * allocSize
                    val maxSize = (Int.MAX_VALUE * 0.95).toInt()
                    if (allocSize >= maxSize) throw RuntimeException("Double array trie is too big.") else resize(
                        Math.min(toSize, maxSize.toDouble()).toInt()
                    )
                }
                if (used!![begin]) continue
                for (i in 1 until siblings.size) if (check[begin + siblings[i].key] != 0) continue@outer
                break
            }

            // -- Simple heuristics --
            // if the percentage of non-empty contents in check between the
            // index
            // 'next_check_pos' and 'check' is greater than some constant value
            // (e.g. 0.9),
            // new 'next_check_pos' index is written by 'check'.
            if (1.0 * nonzero_num / (pos - nextCheckPos + 1) >= 0.95) nextCheckPos =
                pos // 从位置 next_check_pos 开始到 pos 间，如果已占用的空间在95%以上，下次插入节点时，直接从 pos 位置处开始查找
            used!![begin] = true
            size =
                if (size > begin + siblings[siblings.size - 1].key + 1) size else begin + siblings[siblings.size - 1].key + 1
            for (entry in siblings) {
                check[begin + entry.key] = begin
            }
            for (entry in siblings) {
                val new_siblings: MutableList<Map.Entry<Int, State>> =
                    ArrayList<Map.Entry<Int, State>>(
                        entry.value.success.entries.size + 1
                    )
                if (fetch(entry.value, new_siblings) == 0) // 一个词的终止且不为其他词的前缀，其实就是叶子节点
                {
                    base[begin + entry.key] = -entry.value.largestValueId - 1
                    progress++
                } else {
                    siblingQueue.add(
                        AbstractMap.SimpleEntry<Int?, List<Map.Entry<Int, State>>>(
                            begin + entry.key,
                            new_siblings
                        )
                    )
                }
                entry.value.index = begin + entry.key
            }

            // Insert siblings
            if (parentBaseIndex != null) {
                base[parentBaseIndex] = begin
            }
        }

        /**
         * free the unnecessary memory
         */
        fun loseWeight() {
            val nbase = IntArray(size + 65535)
            System.arraycopy(base, 0, nbase, 0, size)
            base = nbase
            val ncheck = IntArray(size + 65535)
            System.arraycopy(check, 0, ncheck, 0, Math.min(check.size, ncheck.size))
            check = ncheck
        }
    }
}