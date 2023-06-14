<template>
  <div>
    <el-dialog title="参数运行" :visible.sync="dialog" width="500px">
      <el-form ref="form" :model="formData" size="small" label-width="100px">
        <el-form-item prop="jobParam" label="任务参数">
          <el-input v-model="formData.jobParam" style="width: 340px;" placeholder="任务参数" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialog = false">取 消</el-button>
        <el-button type="primary" @click="doSubmit()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      dialog: false, formData: {}, handler: function() {}
    }
  },
  methods: {
    runOps(id, handler) {
      this.dialog = true
      this.handler = handler
      this.formData = { id: id }
    },
    doSubmit() {
      this.handler(this.formData).then(res => {
        if (res.code === 200) {
          this.dialog = false
          this.$parent.okTips('运行成功')
        } else {
          this.$parent.failTips(res.message)
        }
      })
    }
  }
}
</script>

<style scoped>
</style>
