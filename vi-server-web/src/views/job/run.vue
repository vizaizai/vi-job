<template>
  <div>
    <el-dialog title="参数运行" :visible.sync="dialog" width="500px">
      <el-form ref="form" :model="formData" size="small" label-width="100px">
        <el-form-item prop="triggerTime" label="触发时间">
          <el-date-picker
            v-model="formData.triggerTime"
            :picker-options="pickerOptions"
            type="datetime"
            placeholder="触发时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item prop="jobParam" label="任务参数">
          <el-input v-model="formData.jobParam" :autosize="{ minRows: 5, maxRows: 15}" type="textarea" style="width: 340px;" placeholder="任务参数" />
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
import { formatDateTime } from '@/utils'
const format = 'yyyy-MM-dd HH:mm:ss'

export default {
  data() {
    return {
      dialog: false,
      handler: function() {},
      formData: {
        triggerTime: null,
        jobParam: null
      },
      pickerOptions: {
        shortcuts: [{
          text: '10秒后',
          onClick(picker) {
            const date = new Date()
            date.setTime(date.getTime() + 1000 * 10)
            picker.$emit('pick', date)
          }
        }, {
          text: '5分钟后',
          onClick(picker) {
            const date = new Date()
            date.setTime(date.getTime() + 1000 * 60 * 5)
            picker.$emit('pick', date)
          }
        }, {
          text: '1小时后',
          onClick(picker) {
            const date = new Date()
            date.setTime(date.getTime() + 1000 * 60 * 60)
            picker.$emit('pick', date)
          }
        }]
      }
    }
  },
  created() {
  },
  methods: {
    runOps(id, handler) {
      this.dialog = true
      this.handler = handler
      this.formData = { triggerTime: formatDateTime(new Date(), format), id: id }
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
