<template>
  <div>
    <el-dialog :title="title" :visible.sync="dialog" width="500px">
      <el-form ref="form" :model="formData" :rules="rules" size="small" label-width="100px">
        <el-form-item prop="name" label="执行器名称">
          <el-input v-model="formData.name" style="width: 340px;" placeholder="执行器名称" />
        </el-form-item>
        <el-form-item prop="appName" label="应用名称">
          <el-input v-model="formData.appName" style="width: 340px;" placeholder="应用名称" :disabled="edit" />
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
      dialog: false, formData: {}, title: '', handler: function() {}, edit: false,
      rules: {
        name: [{ required: true, message: '请输入执行器名称', trigger: 'blur' }],
        appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }]
      }
    }
  },
  methods: {
    editOps(data, handler) {
      this.dialog = true
      this.edit = true
      this.handler = handler
      this.title = '编辑'
      this.formData = { ...data }
    },
    addOps(handler) {
      this.dialog = true
      this.edit = false
      this.handler = handler
      this.title = '添加'
      this.formData = { name: '', appName: '' }
    },
    doSubmit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.handler(this.formData).then(res => {
            if (res.code === 200) {
              this.$parent.toQuery()
              this.dialog = false
              this.$parent.okTips()
            } else {
              this.$parent.failTips(res.message)
            }
          })
        } else {
          this.$parent.failTips('表单信息填写不完整')
        }
      })
    }
  }
}
</script>

<style scoped>
</style>
