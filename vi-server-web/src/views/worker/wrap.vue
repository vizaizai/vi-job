<template>
  <span style="margin: 0 10px">
    <el-button size="mini" icon="el-icon-right" plain @click="dialog = true">
      <span v-if="selectRow">{{ selectRow.name }}</span>
      <span v-else style="color: #C0C4CC">选择执行器</span>
    </el-button>
    <el-dialog title="选择执行器" append-to-body :visible.sync="dialog" width="60%">
      <e-index ref="index" dialog @change="change" />
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialog = false">取 消</el-button>
        <el-button type="primary" @click="doSelect()">确 定</el-button>
      </span>
    </el-dialog>
  </span>
</template>

<script>
import eIndex from './index'
export default {
  components: { eIndex },
  data() {
    return {
      dialog: false,
      changeRow: undefined,
      selectRow: undefined
    }
  },
  methods: {
    init(e) {
      this.selectRow = e
    },
    clear() {
      this.selectRow = null
    },
    change(e) {
      this.changeRow = e
    },
    doSelect() {
      this.selectRow = { ...this.changeRow }
      this.$emit('select', this.selectRow)
      this.dialog = false
    }
  }
}
</script>

<style scoped>
</style>
