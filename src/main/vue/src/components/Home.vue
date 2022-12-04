<template>
    <b-card title="Overview" style="margin:auto;width:fit-content" v-show="showPage">
        <b-alert v-show="retrieveResult !== ''" :variant="errmsg" show>{{ retrieveResult }}</b-alert>
        <b-overlay :show="loading" variant="light" opacity="0.6" rounded="sm"></b-overlay>
        <b-container fluid style="padding-left: 0;">
        <b-row>
          <b-col>
            <label class="sr-only" for="curBranch">Name of the current branch</label>
            <b-form-input id="curBranch" v-model="branchName" class="border"></b-form-input>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <label class="sr-only" for="remoteUrl">SVN location of the current branch</label>
            <b-form-input id="remoteUrl" v-model="remoteUrl" class="border"></b-form-input>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <label class="sr-only mt-2" for="wcLocation">Working Copy location of current Branch</label>
            <b-form-input id="wcLocation" v-model="wcLocation" class="border"></b-form-input>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <label class="sr-only mt-2" for="currentBranchRev">Latest Branch Revision</label>
            <b-form-input id="currentBranchRev" v-model="currentBranchRev" class="border"></b-form-input>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <label class="sr-only mt-2" for="highestTrunkRevInBranch">Highest Trunk Revision in Branch</label>
            <b-form-input id="highestTrunkRevInBranch" v-model="highestTrunkRevInBranch" class="border"></b-form-input>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <b-container fluid style="width:600px;">
              <b-row>
                <b-col cols="3">
                </b-col>
                <b-col md="4">
                  <label class="sr-only mt-2" for="latestBranchRev0">Revision nr in Branch</label>
                </b-col>
                <b-col md="4">
                  <label class="sr-only mt-2" for="latestBranchRev0">Revision nr in Trunk</label>
                </b-col>
              </b-row>
              <b-row>
                <b-col cols="3">
                  <label class="sr-only mt-2 mr-2">flows.json</label>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestBranchRevs[0]" class="border"></b-form-input>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestTrunkRevs[0]" class="border"></b-form-input>
                </b-col>
              </b-row>
              <b-row>
                <b-col cols="3">
                  <label class="sr-only mt-2 mr-2">index.html</label>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestBranchRevs[1]" class="border"></b-form-input>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestTrunkRevs[1]" class="border"></b-form-input>
                </b-col>
              </b-row>
              <b-row>
                <b-col cols="3">
                  <label class="sr-only mt-2 mr-2">index.js</label>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestBranchRevs[2]" class="border"></b-form-input>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestTrunkRevs[2]" class="border"></b-form-input>
                </b-col>
              </b-row>
              <b-row>
                <b-col cols="3">
                  <label class="sr-only mt-2 mr-2">index.css</label>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestBranchRevs[3]" class="border"></b-form-input>
                </b-col>
                <b-col md="4">
                  <b-form-input v-model="latestTrunkRevs[3]" class="border"></b-form-input>
                </b-col>
              </b-row>
            </b-container>
          </b-col>
        </b-row>
      </b-container>
    </b-card>
</template>

<script>
export default {
  name: 'CreateBranch',
  props: ['showPage'],
  data() {
    return {
      curBranchInfo: {},
      branchName: '',
      wcLocation: '',
      currentBranchRev: 0,
      remoteUrl: '',
      latestBranchRevs: [],
      latestTrunkRevs: [],
      highestTrunkRevInBranch: 0,
      loading: false,
      errmsg: '',
      retrieveResult: ''
    }
  },
  methods: {
    getCurrentBranch() {
        this.loading = true;
        this.retrieveResult = "";
        this.errmsg = "success";
        var url = "/api/getbranch";
        this.axios.post(url)
        .then(response => {
            var f = response.data;
            if (String(f).startsWith("svn")) {
              this.retrieveResult = f;
              this.loading = false;
            } else {
              this.curBranchInfo = f;
              this.retrieveResult = "";
              if (f.uptodate) {
                this.retrieveResult = "Branch is up to date";
              }
              this.branchName = f.branchName;
              this.wcLocation = f.wcLocation;
              this.currentBranchRev = f.currentBranchRev;
              this.remoteUrl = f.remoteUrl;
              this.latestBranchRevs = f.latestBranchRevs;
              this.latestTrunkRevs = f.latestTrunkRevs;
              this.highestTrunkRevInBranch = f.highestTrunkRevInBranch;
              this.emitter.emit('SetCurBranch', this.curBranchInfo);
              this.loading = false;
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
    this.getCurrentBranch();
  },
}
</script>
<style>
</style>