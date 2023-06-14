<template>
  <div>
    <el-dialog :title="title" :modal="false" :visible.sync="dialog" width="55%">
      <el-form ref="form" :model="formData" :rules="rules" :disabled="disabled" size="small" label-width="100px">
        <el-form-item prop="name" label="任务名称">
          <el-input v-model="formData.name" style="width: 100%;" placeholder="任务名称" />
        </el-form-item>
        <el-form-item prop="workerId" label="执行器">
          <worker ref="worker" @select="selectWorker" />
        </el-form-item>
        <el-form-item label="生命周期">
          <el-date-picker
            v-model="optRangeTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item prop="processorType" label="处理器">
          <el-select v-model="formData.processorType" placeholder="类型选择" style="width: 27%;margin-right: 3%">
            <el-option
              v-for="item in processorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input v-if="formData.processorType === 1" v-model="formData.processor" style="width: 70%;" placeholder="处理器(@Job的value值)" />
        </el-form-item>
        <el-form-item prop="param" label="任务参数">
          <el-input v-model="formData.param" style="width: 100%;" placeholder="任务参数" />
        </el-form-item>
        <el-form-item prop="triggerType" label="触发策略">
          <el-select v-model="formData.triggerType" placeholder="策略选择" style="width: 27%;margin-right: 3%">
            <el-option
              v-for="item in triggerOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input v-if="formData.triggerType === 1" v-model="formData.cron" style="width: 70%;" placeholder="cron表达式" />
          <el-input v-if="formData.triggerType === 2" v-model="formData.speedS" style="width: 70%;" placeholder="频率（单位：秒）" />
          <el-input v-if="formData.triggerType === 3" v-model="formData.delayedS" style="width: 70%;" placeholder="延时（单位：秒）" />
        </el-form-item>
        <el-form-item prop="routeType" label="路由策略">
          <el-select v-model="formData.routeType" placeholder="策略选择" style="width: 27%;margin-right: 3%">
            <el-option
              v-for="item in routeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="retryCount" label="重试次数">
          <el-input v-model.number="formData.retryCount" style="width: 100%;" placeholder="任务失败重试次数" />
        </el-form-item>
        <el-form-item prop="timeoutS" label="任务超时时间">
          <el-input v-model.number="formData.timeoutS" style="width: 100%;" placeholder="任务超时时间（单位：秒）" />
        </el-form-item>
        <el-form-item v-if="formData.timeoutS != null" prop="timeoutHandleType" label="超时处理方式">
          <el-select v-model="formData.timeoutHandleType" placeholder="处理方式选择" style="width: 27%">
            <el-option
              v-for="item in timeoutOptions"
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
import worker from '../worker/wrap'
export default {
  components: { worker },
  data() {
    return {
      optRangeTime: null,
      dialog: false,
      disabled: false,
      formData: {},
      title: '', handler: function() {},
      rules: {
        name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        workerId: [{ required: true, message: '请选择执行器', trigger: 'change' }],
        processorType: [{ required: true, message: '请选择处理器', trigger: 'blur' }],
        triggerType: [{ required: true, message: '请选择触发策略', trigger: 'blur' }],
        routeType: [{ required: true, message: '请选择路由策略', trigger: 'blur' }],
        retryCount: [{ required: true, message: '请输入重试次数', trigger: 'blur' }]
      },
      processorOptions: [{
        value: 1,
        label: 'BEAN'
      }],
      triggerOptions: [{
        value: 0,
        label: '非主动触发'
      }, {
        value: 1,
        label: 'CRON'
      }, {
        value: 2,
        label: '固定频率'
      }, {
        value: 3,
        label: '固定延时'
      }],
      routeOptions: [{
        value: 1,
        label: '随机'
      }, {
        value: 2,
        label: '故障转移'
      }, {
        value: 3,
        label: '忙碌转移'
      }, {
        value: 4,
        label: '广播'
      }],
      timeoutOptions: [{
        value: 1,
        label: '标记超时'
      }, {
        value: 2,
        label: '中断执行'
      }]
    }
  },
  methods: {
    showOps(data) {
      this.dialog = true
      this.title = '查询详情'
      this.disabled = true
      this.formData = { ...data }
      if (data.startTime && data.endTime) {
        this.optRangeTime = []
        this.optRangeTime.push(data.startTime)
        this.optRangeTime.push(data.endTime)
      }
      this.$nextTick(() => {
        this.$refs.worker.init({ id: data.workerId, name: data.workerName })
      })
    },
    editOps(data, handler) {
      this.dialog = true
      this.handler = handler
      this.title = '编辑任务'
      this.disabled = false
      this.formData = { ...data }
      if (data.startTime && data.endTime) {
        this.optRangeTime = []
        this.optRangeTime.push(data.startTime)
        this.optRangeTime.push(data.endTime)
        delete this.formData.startTime
        delete this.formData.endTime
      }
      this.$nextTick(() => {
        this.$refs.worker.init({ id: data.workerId, name: data.workerName })
      })
    },
    addOps(handler) {
      this.dialog = true
      this.handler = handler
      this.title = '新增任务'
      this.disabled = false
      this.optRangeTime = null
      this.formData = { retryCount: 0 }
      this.$nextTick(() => {
        this.$refs.worker.clear()
      })
    },
    doSubmit() {
      if (this.disabled) {
        this.dialog = false
        return
      }
      if (this.optRangeTime) {
        this.formData.startTime = this.optRangeTime[0]
        this.formData.endTime = this.optRangeTime[1]
      }
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
    },
    selectWorker(e) {
      this.formData.workerId = e.id
    }
  }
}
</script>

<style scoped>
</style>
