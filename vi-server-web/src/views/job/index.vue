<template>
  <div class="app-container">
    <!--表单-->
    <e-form ref="form" />
    <!-- 运行弹窗 -->
    <e-run ref="run" />
    <!--工具栏-->
    <div class="head-container">
      <el-input v-model="query.name" clearable size="mini" placeholder="请输入任务名称" style="width: 200px;" class="filter-item" @keyup.enter.native="toQuery" />
      <worker ref="worker" style="margin: 0 10px" @select="selectWorker" />
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-search" @click="toQuery">搜索</el-button>
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-refresh-left" @click="resetQuery()">重置</el-button>
      <el-button type="primary" size="mini" icon="el-icon-plus" @click="toAdd">新增任务</el-button>
    </div>
    <div class="table-container">
      <!--表格-->
      <el-table :data="data" width="100%">
        <el-table-column
          prop="id"
          label="任务ID"
          width="100"
        />
        <el-table-column
          prop="name"
          label="任务名称"
          width="200"
        />
        <el-table-column
          prop="workerName"
          label="执行器"
          width="200"
        />
        <el-table-column
          label="处理器类型"
          width="120"
        >
          <template v-slot="{ row }">
            {{ row.processorType === 1 ? 'Bean' : 'HTTP' }}
          </template>
        </el-table-column>
        <el-table-column
          label="触发类型"
          width="180"
        >
          <template v-slot="{ row }">
            {{ getTriggerType(row) }}
          </template>
        </el-table-column>
        <el-table-column
          label="生命周期"
          width="200"
        >
          <template v-slot="{ row }">
            <div v-if="row.startTime">起: {{ row.startTime }}</div>
            <div v-if="row.endTime">止: {{ row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column
          label="任务状态"
          width="130"
        >
          <template v-slot="{ row }">
            <el-tag v-if="row.status === 1" type="success">运行中</el-tag>
            <el-tag v-else type="danger">停止</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          width="180"
          label="创建时间"
        />
        <el-table-column
          label="操作"
          width="%100"
        >
          <template v-slot="scope">
            <el-button type="text" size="small" @click="toUpdateStatus(scope.row)">{{ scope.row.status === 1 ? '停止' : '开启' }}</el-button>
            <el-button type="text" size="small" @click="toLook(scope.row)">详情</el-button>
            <span style="margin-left: 10px">
              <el-dropdown :hide-on-click="false">
                <span class="el-dropdown-link">
                  更多<i class="el-icon-arrow-down el-icon--right" />
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item class="dropdown-item" @click.native="toEdit(scope.row)">编辑</el-dropdown-item>
                  <el-dropdown-item class="dropdown-item" @click.native="toRun(scope.row)">运行</el-dropdown-item>
                  <el-dropdown-item class="dropdown-item" @click.native="toRecord(scope.row)">调度记录</el-dropdown-item>
                  <el-dropdown-item class="dropdown-item" @click.native="toCopy(scope.row)">复制</el-dropdown-item>
                  <el-dropdown-item divided class="dropdown-item" @click.native="toDel(scope.row)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
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
import { page, remove, add, update, updateStatus, run } from '@/api/job'
import initData from '@/mixins/initData'
import eForm from './form'
import eRun from './run'
import worker from '../worker/wrap'

export default {
  name: 'Worker',
  components: { eForm, worker, eRun },
  mixins: [initData],
  data() {
    return {
      invoke: page,
      query: {
        name: null,
        workerId: null
      }
    }
  },
  created() {
    this.toQuery()
  },
  methods: {
    resetQuery() {
      this.query = {}
      this.init()
      this.$refs.worker.clear()
    },
    toUpdateStatus(row) {
      const params = { id: row.id, status: row.status === 1 ? 0 : 1 }
      updateStatus(params).then(res => {
        if (res.code === 200) {
          this.okTips(params.status === 1 ? '已开启' : '已停止')
          this.toQuery()
        } else {
          this.failTips(res.message)
        }
      })
    },
    toLook(row) {
      this.$refs.form.showOps(row, update)
    },
    toRun(row) {
      this.$refs.run.runOps(row.id, run)
    },
    toCopy(row) {
      const job = { ...row }
      delete job.id
      this.$refs.form.editOps(job, add, '新增任务')
    },
    toEdit(row) {
      this.$refs.form.editOps(row, update)
    },
    toAdd() {
      this.$refs.form.addOps(add)
    },
    toRecord(row) {
      this.$router.push({
        name: 'Record',
        query: { jobId: row.id }
      })
    },
    toDel(row) {
      const data = { id: row.id }
      this.confirm('确认删除?', data, (data) => {
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
    getTriggerType(row) {
      switch (row.triggerType) {
        case 0:
          return '非主动触发'
        case 1:
          return 'CRON（' + row.cron + '）'
        case 2:
          return '固定频率（' + row.speedS + 's）'
        case 3:
          return '固定延时（' + row.delayedS + 's）'
        default:
          return ''
      }
    },
    selectWorker(e) {
      this.query.workerId = e.id
      this.toQuery()
    }
  }
}
</script>

<style scoped>
.head-container,.table-container{
  margin-top: 25px;
}
.el-dropdown-link {
  font-size: 12px;
  cursor: pointer;
  color: #409EFF;
}
.dropdown-item {
  font-size: 12px;
  cursor: pointer;
}
.el-icon-arrow-down {
  font-size: 12px;
}
</style>
