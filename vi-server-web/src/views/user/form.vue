<template>
  <div>
    <el-dialog :title="title" :visible.sync="dialog" width="500px">
      <el-form ref="form" :model="formData" :rules="rules" size="small" label-width="100px">
        <el-form-item prop="userName" label="用户名">
          <el-input v-model="formData.userName" style="width: 340px;" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input v-model="formData.password" style="width: 340px;" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item prop="role" label="角色">
          <el-select v-model="formData.role" placeholder="请选择角色">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
      options: [{
        value: 1,
        label: '管理员'
      }, {
        value: 2,
        label: '普通用户'
      }],
      dialog: false, formData: {}, title: '', handler: function() {}, edit: false,
      rules: {
        userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'blur' }]
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
      this.formData = { }
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
