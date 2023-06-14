<template>
  <div class="app-container">
    <!--表单-->
    <e-form ref="form" />
    <!--工具栏-->
    <div class="head-container">
      <el-input v-model="query.userName" clearable size="small" placeholder="请输入用户名" style="width: 200px; margin-right: 5px" class="filter-item" @keyup.enter.native="toQuery" />
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-search" @click="toQuery">搜索</el-button>
      <el-button class="filter-item" size="mini" type="primary" icon="el-icon-refresh-left" @click="resetQuery()">重置</el-button>
      <el-button type="primary" size="mini" icon="el-icon-plus" @click="toAdd">新增用户</el-button>
    </div>
    <div class="table-container">
      <!--表格-->
      <el-table :data="data" width="100%">
        <el-table-column
          prop="userName"
          label="用户名"
          width="280"
        />
        <el-table-column
          label="角色"
          width="280"
        >
          <template v-slot="{ row }">
            {{ row.role === 1 ? '管理员' : '普通用户' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          width="180"
          label="注册时间"
        />
        <el-table-column
          label="操作"
          width="%100"
        >
          <template v-slot="scope">
            <!--            <el-button type="text" size="small" @click="toEdit(scope.row)">编辑</el-button>-->
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
import { page, remove, addSysUser } from '@/api/user'
import initData from '@/mixins/initData'
import eForm from './form'

export default {
  name: 'Worker',
  components: { eForm },
  mixins: [initData],
  data() {
    return {
      invoke: page,
      query: {
        userName: null
      }
    }
  },
  created() {
    this.toQuery()
  },
  methods: {
    toEdit(row) {
      // this.$refs.form.editOps(row, saveOrUpdate)
    },
    toAdd() {
      this.$refs.form.addOps(addSysUser)
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
    }
  }
}
</script>

<style scoped>
.head-container,.table-container{
  margin-top: 30px;
}
</style>
