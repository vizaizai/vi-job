<template>
  <div class="app-container">
    <!--表单-->
    <e-form ref="form" />
    <!-- 集群节点 -->
    <el-dialog
      title="集群节点"
      :visible.sync="showCluster"
      width="30%"
    >
      <div>
        <div style="margin: 10px auto; width: 200px">
          <div v-for="node in nodes" :key="node.id" style="margin-top: 5px">
            <svg-icon :icon-class="node.online === 1 ? 'online' : 'offline'" /> <span>{{ node.address }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="text" size="mini" @click="showCluster = false">我知道了</el-button>
      </span>
    </el-dialog>
    <!--工具栏-->
    <div v-if="!dialog" class="head-container">
      <el-input v-model="query.appName" clearable size="small" placeholder="请输入应用名称" style="width: 200px; margin-right: 5px" class="filter-item" @keyup.enter.native="toQuery" />
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-search" @click="toQuery">搜索</el-button>
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-refresh-left" @click="resetQuery()">重置</el-button>
      <el-button type="primary" size="mini" icon="el-icon-plus" @click="toAdd">新增执行器</el-button>
    </div>
    <div class="table-container">
      <!--表格-->
      <el-table :data="data" width="100%" :highlight-current-row="dialog" @current-change="handleCurrentChange">
        <el-table-column
          prop="id"
          label="id"
          width="180"
        />
        <el-table-column
          prop="name"
          label="执行器名称"
          width="280"
        />
        <el-table-column
          prop="appName"
          label="应用名称"
          width="280"
        />
        <el-table-column
          prop="createTime"
          width="180"
          label="创建时间"
        />
        <el-table-column
          v-if="!dialog"
          label="操作"
          width="%100"
        >
          <template v-slot="scope">
            <el-button type="text" size="small" @click="toCluster(scope.row)">集群节点</el-button>
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
import { page, saveOrUpdate, remove, nodes } from '@/api/worker'
import initData from '@/mixins/initData'
import eForm from './form'

export default {
  name: 'Worker',
  components: { eForm },
  mixins: [initData],
  props: {
    dialog: Boolean
  },
  data() {
    return {
      nodes: [],
      showCluster: false,
      invoke: page,
      query: {
        appName: null
      },
      currentRow: null
    }
  },
  created() {
    this.toQuery()
  },
  methods: {
    toCluster(row) {
      nodes(row.id).then(response => {
        this.nodes = response.data
        this.showCluster = true
      })
    },
    toEdit(row) {
      this.$refs.form.editOps(row, saveOrUpdate)
    },
    toAdd() {
      this.$refs.form.addOps(saveOrUpdate)
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
    handleCurrentChange(row) {
      this.$emit('change', row)
    }
  }
}
</script>

<style scoped>
.head-container,.table-container{
  margin-top: 30px;
}
</style>
