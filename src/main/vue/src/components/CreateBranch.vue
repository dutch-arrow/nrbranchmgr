<template>
  <b-card title="Create a new Branch" style="margin:auto;width:fit-content" v-show="showPage">
    <b-form>
      <b-container fluid style="padding-left: 0;">
        <b-row>
          <b-col>
            <b-form-checkbox id="cb1" class="mb-2" v-model="remCurBranch" value=false unchecked-value="keep">Remove current branch?</b-form-checkbox>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <label class="sr-only mt-2" for="newBranchName">Name of new branch</label>
            <b-form-input v-model="newBranchName" class="border"></b-form-input>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <b-button style="width:100%;" v-on:click="createBranch" class="mt-2" variant="primary">Create Branch</b-button>
          </b-col>
        </b-row>
        <b-overlay :show="loading" variant="light" opacity="0.6" rounded="sm"></b-overlay>
        <b-row v-show="retrieveResult !== ''">
          <b-col class="mt-3">
            <b-alert :variant=errmsg show>{{ retrieveResult }}</b-alert>
          </b-col>
        </b-row>
      </b-container>
    </b-form>
  </b-card>
</template>

<script>
export default {
  name: 'CreateBranch',
  props: ['showPage'],
  data() {
    return {
      remCurBranch: false,
      curBranchName: '',
      newBranchName: '',
      loading: false,
      errmsg: '',
      retrieveResult: ''
    }
  },
  methods: {
    /*
    * Page refresh
    */
    refresh() {
      window.location.reload();
    },
    createBranch() {
      this.loading = true;
      this.retrieveResult = "";
      this.errmsg = "success";
      var url = "/api/createbranch/" + this.newBranchName
      if (this.remCurBranch) {
        url += ":" + this.curBranchName;
      }
      this.axios.post(url)
        .then(response => {
          var f = response.data;
          if (String(f).length > 0) {
            this.errmsg = "danger";
            this.retrieveResult = f;
          } else {
            this.retrieveResult = "";
            this.curBranchName = this.newBranchName;
            this.refresh();
          }
        })
        .catch(error => {
          this.retrieveResult = error.message;
          this.errmsg = "danger";
          this.loading = false;
       });
    }
  },
  mounted() {

  }
}
</script>

<style scoped>
</style>
