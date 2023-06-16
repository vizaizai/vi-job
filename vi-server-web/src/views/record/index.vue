<template>
  <div class="app-container">
    <!--工具栏-->
    <div v-if="!dialog" class="head-container">
      <el-input v-model="query.jobId" clearable size="mini" placeholder="请输入任务ID" style="width: 200px" class="filter-item" @keyup.enter.native="toQuery" />
      <worker ref="worker" style="margin-left: 10px" @select="selectWorker" />
      <el-select v-model="query.dispatchStatus" placeholder="调度状态" size="mini" style="margin-left: 10px">
        <el-option
          v-for="item in triggerOpts"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select v-model="query.executeStatus" placeholder="执行状态" size="mini" style="margin-left: 10px">
        <el-option
          v-for="item in executeOpts"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-date-picker
        v-model="rangeTime"
        type="datetimerange"
        size="mini"
        range-separator="至"
        start-placeholder="调度开始时间"
        end-placeholder="调度结束时间"
        style="margin-left: 10px"
        value-format="yyyy-MM-dd HH:mm:ss"
      />
      <el-button class="filter-item" style="margin-left: 10px" size="mini" type="primary" icon="el-icon-search" @click="toQuery">搜索</el-button>
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-refresh-left" @click="resetQuery()">重置</el-button>
    </div>
    <div class="table-container">
      <!--表格-->
      <el-table :data="data" width="100%">
        <el-table-column
          prop="id"
          label="ID"
          width="100"
        />
        <el-table-column
          label="任务"
          width="180"
        >
          <template v-slot="{ row }">
            <span>#{{ row.jobId }}</span>&nbsp;<span v-if="row.jobName" style="color: #909399">{{ row.jobName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="执行器"
          width="180"
        >
          <template v-slot="{ row }">
            <span>#{{ row.workerId }}</span>
            &nbsp;<span v-if="row.workerName" style="color: #909399">{{ row.workerName }}</span>
            <div v-if="row.workerAddress" style="color: #409EFF">{{ row.workerAddress }}</div>
          </template>
        </el-table-column>
        <el-table-column
          label="触发状态"
          width="180"
        >
          <template v-slot="{ row }">
            <el-tooltip class="item" effect="dark" :content="row.errorMsg" placement="top">
              <el-tag v-if="row.dispatchStatus === 0" type="danger">调度失败</el-tag>
            </el-tooltip>
            <el-tag v-if="row.dispatchStatus === 1" type="success">调度成功</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="triggerTime"
          label="触发时间"
          width="180"
        />
        <el-table-column
          label="执行状态"
          width="180"
        >
          <template v-slot="{ row }">
            <el-tag v-if="row.executeStatus === 0" type="danger">执行失败</el-tag>
            <el-tag v-else-if="row.executeStatus === 1" type="">执行中</el-tag>
            <el-tag v-else-if="row.executeStatus === 2" type="success">执行成功</el-tag>
            <el-tag v-else-if="row.executeStatus === 3" type="warning">执行成功（超时）</el-tag>
            <el-tag v-else-if="row.executeStatus === 4" type="warning">超时中断</el-tag>
            <el-tag v-else-if="row.executeStatus === 5" type="warning">主动中断</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="执行时间"
          width="230"
        >
          <template v-slot="{ row }">
            {{ row.executeStartTime }} - {{ row.executeEndTime }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="%100"
        >
          <template v-slot="scope">
            <el-button v-if="scope.row.executeStatus === 1" type="text" size="small" @click="toKill(scope.row)">中断执行</el-button>
            <el-button type="text" size="small" @click="toLog(scope.row)">执行日志</el-button>
            <el-button type="text" size="small" @click="toDel(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!--分页组件-->
    <el-pagination
      :page-size.sync="limit"
      :total="total"
      :current-page.sync="page"
      :page-sizes="[10, 20, 50, 100]"
      style="margin-top: 8px;"
      layout="total, prev, pager, next, sizes"
      @size-change="sizeChangeHandler"
      @current-change="pageChangeHandler"
    />
  </div>
</template>
<script>
import { getLog, kill, page, remove } from '@/api/dispatch'
import { formatDateTime } from '@/utils'
import initData from '@/mixins/initData'
import worker from '../worker/wrap'
const format = 'yyyy-MM-dd HH:mm:ss'
export default {
  name: 'Record',
  components: { worker },
  mixins: [initData],
  props: {
    dialog: Boolean
  },
  data() {
    return {
      rangeTime: this.getInitTime(),
      invoke: page,
      query: {
        jobId: null
      },
      triggerOpts: [{
        value: 1,
        label: '调度成功'
      }, {
        value: 0,
        label: '调度失败'
      }],
      executeOpts: [{
        value: 0,
        label: '执行失败'
      }, {
        value: 1,
        label: '执行中'
      }, {
        value: 2,
        label: '执行成功'
      }, {
        value: 3,
        label: '执行成功（超时）'
      }, {
        value: 4,
        label: '超时中断'
      }, {
        value: 5,
        label: '主动中断'
      }]
    }
  },
  created() {
    if (this.$route.query.jobId) {
      this.query.jobId = this.$route.query.jobId
    }
    this.toQuery()
  },
  methods: {
    beforeInit() {
      if (this.rangeTime && this.rangeTime.length === 2) {
        console.log(this.rangeTime)
        this.query.triggerStartTime = this.rangeTime[0]
        this.query.triggerEndTime = this.rangeTime[1]
      }
      return true
    },
    resetQuery() {
      this.query = {}
      this.rangeTime = this.getInitTime()
      this.init()
      this.$refs.worker.clear()
    },
    toKill(row) {
      this.confirm('确认中断?', { id: row.id }, (data) => {
        kill(data).then(res => {
          if (res.code === 200) {
            this.okTips('中断成功')
            this.toQuery()
          } else {
            this.failTips(res.message)
          }
        })
      })
    },
    toLog(row) {
      getLog({ id: row.id }).then(res => {
        if (res.code === 200) {
          console.log(res)
        } else {
          this.failTips(res.message)
        }
      })
    },
    toDel(row) {
      this.confirm('确认删除?', { id: row.id }, (data) => {
        remove(data).then(res => {
          if (res.code === 200) {
            this.okTips('删除成功')
            this.toQuery()
          } else {
            this.failTips(res.message)
          }
        })
      })
    },
    selectWorker(e) {
      this.query.workerId = e.id
      this.toQuery()
    },
    getInitTime() {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [formatDateTime(start, format), formatDateTime(end, format)]
    }
  }
}
</script>

<style scoped>
.head-container,.table-container{
  margin-top: 30px;
}
</style>
