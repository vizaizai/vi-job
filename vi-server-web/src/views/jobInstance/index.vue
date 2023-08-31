<template>
  <div ref="container" class="app-container">
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
      <el-table
        :data="data"
        width="100%"
        row-key="id"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        :row-class-name="tableRowClassName"
      >
        <el-table-column
          prop="id"
          label="ID"
          width="130"
        >
          <template v-slot="{ row }">
            {{ row.id }}
          </template>
        </el-table-column>
        <el-table-column
          label="任务"
          width="180"
        >
          <template v-slot="{ row }">
            <span v-if="row.pid === 0">
              <span>#{{ row.jobId }}</span>&nbsp;<span v-if="row.jobName" style="color: #909399">{{ row.jobName }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          label="执行器"
          width="180"
        >
          <template v-slot="{ row }">
            <span v-if="row.pid === 0">
              <span>#{{ row.workerId }}</span>&nbsp;
              <span v-if="row.workerName" style="color: #909399">{{ row.workerName }}</span>
            </span>
            <div v-if="row.workerAddress" style="color: #409EFF">{{ row.workerAddress }}</div>
          </template>
        </el-table-column>
        <el-table-column
          label="调度状态"
          width="100"
        >
          <template v-slot="{ row }">
            <el-tag v-if="row.dispatchStatus === 0" type="info">等待调度</el-tag>
            <el-tag v-if="row.dispatchStatus === 1" type="success">调度成功</el-tag>
            <el-tooltip v-if="row.dispatchStatus === 2" class="item" effect="dark" :content="row.errorMsg" placement="top">
              <el-tag type="danger">调度失败</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="triggerTime"
          label="触发时间"
          width="160"
        >
          <template v-slot="{ row }">
            {{ row.triggerTime }} <span v-if="row.dispatchStatus === 0">(预计)</span>
          </template>
        </el-table-column>
        <el-table-column
          label="执行状态"
          width="120"
        >
          <template v-slot="{ row }">
            <el-tag v-if="row.executeStatus === 0" type="danger">执行失败</el-tag>
            <el-tag v-else-if="row.executeStatus === 1" type="">执行中<span v-if="row.execStatus === 2">（等待）</span></el-tag>
            <el-tag v-else-if="row.executeStatus === 2" type="success">执行成功</el-tag>
            <el-tag v-else-if="row.executeStatus === 3" type="warning">执行超时</el-tag>
            <el-tag v-else-if="row.executeStatus === 4" type="info">执行取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="执行时间"
          width="330"
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
            <el-button v-if="scope.row.executeStatus === 1 && scope.row.execStatus === 2" type="text" size="small" @click="toCancel(scope.row)">取消</el-button>
            <el-button v-if="scope.row.dispatchStatus === 1" type="text" size="small" @click="toLog(scope.row)">执行日志</el-button>
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
    <!-- 日志 -->
    <el-drawer
      ref="logDrawer"
      title="执行日志"
      :visible.sync="logDrawer.show"
      :modal="false"
      :close-on-press-escape="false"
      :wrapper-closable="false"
      :show-close="true"
      :size="sizePercent"
      direction="btt"
      @opened="logDrawerOpened"
      @closed="logDrawerClosed"
    >
      <div slot="title">
        <div ref="drawerBar" class="drawer-top" />
      </div>
      <div style="height: 100%">
        <el-tabs v-model="logDrawer.logTab" type="card" closable @tab-remove="removeLogTab">
          <el-tab-pane
            v-for="(item) in logDrawer.logTabs"
            :key="item.name"
            :label="item.title"
            :name="item.name"
          >
            <el-container :style="{ 'margin-left': '10px', 'height': logContentHeight }">
              <el-main>
                <el-scrollbar :ref="'logScroll_' + item.name" style="height: 100%;">
                  <span v-for="(content, index) in item.contents" :key="index" style="color: black; white-space: pre-line; font-size: 15px; line-height: 13px">{{ content }}</span>
                </el-scrollbar>
              </el-main>
              <el-footer height="30px">
                <el-button style="position: absolute; right: 90px" size="mini" type="primary" @click="scrollToBottom">回到底部</el-button>
                <el-button style="position: absolute; right: 10px" size="mini" type="primary" @click="queryLog">刷新</el-button>
              </el-footer>
            </el-container>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>
<script>
import { getLog, cancel, page, remove } from '@/api/jobInstance'
import { formatDateTime } from '@/utils'
import initData from '@/mixins/initData'
import worker from '../worker/wrap'

const format = 'yyyy-MM-dd HH:mm:ss'
export default {
  name: 'JobInstance',
  components: { worker },
  mixins: [initData],
  props: {
    dialog: Boolean
  },
  data() {
    return {
      logDrawer: {
        show: false,
        isDown: false,
        radio: 0.5,
        logTab: '',
        logTabs: [],
        timeoutId: null
      },
      rangeTime: this.getInitTime(),
      invoke: page,
      query: {
        jobId: null
      },
      triggerOpts: [{
        value: 0,
        label: '等待调度'
      }, {
        value: 1,
        label: '调度成功'
      }, {
        value: 2,
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
        label: '执行超时'
      }, {
        value: 4,
        label: '执行取消'
      }]
    }
  },
  computed: {
    sizePercent() {
      return this.logDrawer.radio * 100 + '%'
    },
    logContentHeight() {
      return this.logDrawer.radio * document.body.clientHeight - 158 + 'px'
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
    toCancel(row) {
      this.confirm('确认取消?', { id: row.id }, (data) => {
        cancel(data).then(res => {
          if (res.code === 200) {
            this.okTips('取消成功')
            this.toQuery()
          } else {
            this.failTips(res.message)
          }
        })
      })
    },
    toLog(row) {
      this.logDrawer.show = true
      const title = row.jobName + '_' + row.id
      const id = row.id.toString()
      this.logDrawer.logTab = id
      for (const tab of this.logDrawer.logTabs) {
        if (tab.name === id) {
          return
        }
      }
      this.logDrawer.logTabs.push(
        {
          title: title,
          name: id,
          pos: 0,
          contents: []
        }
      )
      this.queryLog()
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
    queryLog() {
      const tab = this.logDrawer.logTabs.find((e) => {
        if (e.name === this.logDrawer.logTab) {
          return e
        }
      })
      if (tab) {
        getLog({ id: parseInt(this.logDrawer.logTab), maxLines: 500, startPos: tab.pos }).then(res => {
          if (res.code === 200) {
            if (res.data && res.data.data) {
              // 追加显示数据
              if (!tab.contents.find(e => e === res.data.data)) {
                tab.contents.push(res.data.data)
              }
              tab.pos = res.data.endPos
              // 滚动条到底
              this.scrollToBottom()
              // 继续查询
              this.logDrawer.timeoutId = setTimeout(this.queryLog, 1000)
            }
          } else {
            this.failTips(res.message)
          }
        })
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const logScroll = this.$refs['logScroll_' + this.logDrawer.logTab][0]
        logScroll.wrap.scrollTop = logScroll.wrap.scrollHeight
      })
    },
    selectWorker(e) {
      this.query.workerId = e.id
      this.toQuery()
    },
    getInitTime() {
      const end = new Date()
      end.setTime(end.getTime() + 3600 * 1000 * 24 * 3)
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [formatDateTime(start, format), formatDateTime(end, format)]
    },
    removeLogTab(targetName) {
      const tabs = this.logDrawer.logTabs
      let activeName = this.logDrawer.logTab
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            const nextTab = tabs[index + 1] || tabs[index - 1]
            if (nextTab) {
              activeName = nextTab.name
            }
          }
        })
      }
      this.logDrawer.logTab = activeName
      this.logDrawer.logTabs = tabs.filter(tab => tab.name !== targetName)
      if (this.logDrawer.logTabs.length === 0) {
        this.logDrawer.show = false
      }
    },
    isMoveBar(clientX, clientY) {
      const { top, bottom, left, right } = this.$refs.drawerBar.getBoundingClientRect()
      return clientX >= left && clientX <= right && clientY >= top && clientY <= bottom
    },
    logDrawerOpened() {
      const container = this.$refs.container
      container.addEventListener('mousedown', this.barMousedown)
      container.addEventListener('mouseup', this.barMouseup)
      var element = document.querySelector('.el-drawer')
      element.style.pointerEvents = 'auto'
      element.parentNode.style.pointerEvents = 'none'
      element.parentNode.parentNode.style.pointerEvents = 'none'
    },
    logDrawerClosed() {
      const container = this.$refs.container
      container.removeEventListener('mousedown', this.barMousedown)
      container.removeEventListener('mousemove', this.barMousemove)
      container.removeEventListener('mouseup', this.barMouseup)
      if (this.logDrawer.timeoutId) {
        clearTimeout(this.logDrawer.timeoutId)
      }
    },
    barMousedown({ clientX, clientY }) {
      if (this.isMoveBar(clientX, clientY)) {
        this.logDrawer.isDown = true
        const container = this.$refs.container
        container.addEventListener('mousemove', this.barMousemove)
      }
    },
    barMousemove({ clientY }) {
      if (this.logDrawer.isDown) {
        const height = document.body.clientHeight
        const radio = (height - (clientY - 32)) / height
        this.logDrawer.radio = radio.toFixed(2)
      }
    },
    barMouseup(e) {
      this.logDrawer.isDown = false
    },
    tableRowClassName({ row, rowIndex }) {
      if (row.pid === 0) {
        return ''
      }
      return 'children'
    }
  }
}
</script>

<style scoped>
.head-container,
.table-container {
  margin-top: 30px;
}

.drawer-top {
  width: 100px;
  height: 5px;
  background-color: darkgray;
  margin: 0 auto;
  border-radius: 3px;
  cursor: ns-resize;
}

/deep/ .el-scrollbar__wrap {
  overflow-x: hidden;
}
</style>
