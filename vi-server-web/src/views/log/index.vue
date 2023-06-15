<template>
  <div class="app-container">
    <!--工具栏-->
    <div v-if="!dialog" class="head-container">
      <el-input v-model="query.appName" clearable size="small" placeholder="请输入应用名称" style="width: 200px; margin-right: 5px" class="filter-item" @keyup.enter.native="toQuery" />
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-search" @click="toQuery">搜索</el-button>
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-refresh-left" @click="resetQuery()">重置</el-button>
      <el-button type="primary" size="mini" icon="el-icon-plus" @click="toAdd">新增执行器</el-button>
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
          prop="triggerTime"
          label="触发时间"
          width="180"
        />
        <el-table-column
          label="触发状态"
          width="180"
        >
          <template v-slot="{ row }">
            <el-tooltip class="item" effect="dark" :content="row.errorMsg" placement="top">
              <el-tag v-if="row.dispatchStatus === 0" type="danger">失败</el-tag>
            </el-tooltip>
            <el-tag v-if="row.dispatchStatus === 1" type="success">成功</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="%100"
        >
          <template v-slot="scope">
            <el-button type="text" size="small" @click="toEdit(scope.row)">编辑</el-button>
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
import { pageLogs } from '@/api/job'
import initData from '@/mixins/initData'

export default {
  name: 'Log',
  mixins: [initData],
  props: {
    dialog: Boolean
  },
  data() {
    return {
      nodes: [],
      showCluster: false,
      invoke: pageLogs,
      query: {
        appName: null
      }
    }
  },
  created() {
    this.toQuery()
  },
  methods: {
    toEdit(row) {
    },
    toAdd() {
    },
    toDel(row) {
      const data = { id: row.id }
      this.confirm('确认删除?', data, (data) => {
        pageLogs(data).then(res => {
          if (res.code === 200) {
            this.okTips('删除成功')
            this.toQuery()
          } else {
            this.failTips(res.message)
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.head-container,.table-container{
  margin-top: 30px;
}
</style>
