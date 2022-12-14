<template>
    <b-card title="Overview" style="margin:auto;width:fit-content" v-show="showPage">
        <b-alert v-show="retrieveResult !== ''" :variant="errmsg" show>{{ retrieveResult }}</b-alert>
        <b-overlay :show="loading" variant="light" opacity="0.6" rounded="sm"></b-overlay>
        <b-container fluid style="padding-left: 0;" v-show="isDirty === 0">
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
        <b-row class="mt-2">
          <b-col>
            <b-button v-show="showMergeFlowsButton" variant="primary" class="w-100 me-1" v-on:click="mergeFlows">Merge Flow</b-button>
          </b-col>
          <b-col>
            <b-button v-show="showMergeUiButton" variant="primary"  class="w-100 me-1"  v-on:click="mergeUi">Merge Ui</b-button>
          </b-col>
          <b-col>
            <b-button v-show="showUpdateButton" variant="primary"  class="w-100"  v-on:click="update">Update</b-button>
          </b-col>
        </b-row>
      </b-container>
      <b-alert v-show="buttonResult !== ''" :variant="errmsg" show>{{ buttonResult }}</b-alert>
      <b-overlay :show="loadingButton" variant="light" opacity="0.6" rounded="sm"></b-overlay>
      <b-container fluid style="padding-left: 0;width:600px;" v-show="isDirty > 0">
        <b-row>
          <b-col>
            <label class="sr-only mt-2">Commit message</label>
          </b-col>
        </b-row>
        <b-row>
          <b-col cols="10">
            <b-form-input v-model="commitMsg" class="border"></b-form-input>
          </b-col>
          <b-col cols="2">
            <b-button variant="primary" v-on:click="commit">Commit</b-button>
          </b-col>
        </b-row>
      </b-container>
    </b-card>
    <merge-trunk-flow :showPage="showMergeFlowsPage"></merge-trunk-flow>
    <merge-trunk-ui :showPage="showMergeUiPage"></merge-trunk-ui>
</template>

<script>
import MergeTrunkFlow from './MergeTrunkFlow.vue'
import MergeTrunkUi from './MergeTrunkUi.vue'

export default {
  components: {
    MergeTrunkFlow,
    MergeTrunkUi,
  },
  name: 'HomePage',
  props: ['showPage'],
  data() {
    return {
      loading: false,
      errmsg: '',
      retrieveResult: '',
      buttonResult: '',
      loadingButton: '',
      isDirty: -1,
      commitMsg: '',
      branchName: '',
      branchInfo: {
        dirty: false,
        branchName: '', 
        wcLocation: '', 
        remoteUrl: '', 
        currentBranchRev: 0, 
        latestBranchRevs:[0,0,0,0], 
        latestTrunkRevs:[0,0,0,0],
        highestTrunkRevInBranch: 0,
      },
      wcLocation: '',
      currentBranchRev: 0,
      remoteUrl: '',
      latestBranchRevs: [],
      latestTrunkRevs: [],
      highestTrunkRevInBranch: 0,
      showMergeFlowsPage: false,
      showMergeUiPage: false,
      showMergeFlowsButton: false,
      showMergeUiButton: false,
      showUpdateButton: false,
      flowsMerged: false,
      htmlMerged: false,
      jsMerged: false,
      cssMerged: false,
      allMerged: false,
      changedNodes: {},
      changedHtml: {},
      changedJs: {},
      changedCss: {},
     }
  },
  methods: {
    getCurrentBranch() {
        this.loading = true;
        this.retrieveResult = "";
        this.buttonResult = '';
        this.loadingButton = false;
        this.errmsg = "success";
        this.showUpdateButton = true;
        this.axios.post("/api/getbranch")
        .then(response => {
            var f = response.data;
            console.log(f);
            if (String(f).startsWith("svn")) {
              this.retrieveResult = f;
              this.loading = false;
            } else {
              this.retrieveResult = "";
              if (f.uptodate) {
                this.retrieveResult = "Branch is up to date";
                this.showUpdateButton = false;
              } else {
                this.retrieveResult = "Branch must merge the latest trunk.";
              }
              if (f.dirty > 0) {
                this.retrieveResult = "Branch has not been commited. Please commit first.";
                this.errmsg = "danger";
              }
              this.branchInfo = f;
              this.isDirty = f.dirty;
              this.branchName = f.branchName;
              this.wcLocation = f.wcLocation;
              this.currentBranchRev = f.currentBranchRev;
              this.remoteUrl = f.remoteUrl;
              this.latestBranchRevs = f.latestBranchRevs;
              this.latestTrunkRevs = f.latestTrunkRevs;
              this.highestTrunkRevInBranch = f.highestTrunkRevInBranch;
              this.emitter.emit('SetCurBranch', {branchName: this.branchName});
              this.emitter.emit("MergeFlow", this.branchInfo);
              this.emitter.emit('MergeUi', this.branchInfo);
              this.loading = false;
            }
        })
        .catch(error => {
          console.log("Error:", error);
            this.retrieveResult = error.message;
            this.errmsg = "danger";
            this.loading = false;
        });
    },
    commit() {
      this.loading = true;
      this.retrieveResult = "";
      this.errmsg = "success";
      this.axios.post("/api/commitbranch/" + this.commitMsg)
      .then(response => {
        var f = response.data;
        if (String(f).startsWith("svn")) {
          this.retrieveResult = f;
          this.loading = false;
        } else {
          this.getCurrentBranch();
        }
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    mergeFlows() {
      this.showMergeFlowsPage = true;
      this.showMergeUiPage = false;
    },
    mergeUi() {
      this.showMergeFlowsPage = false;
      this.showMergeUiPage = true;
    },
    update() {
      if (this.showMergeFlowsButton) {
        this.emitter.emit("AreFlowsMerged");
      }
      if (this.showMergeUiButton) {
       this.emitter.emit("IsUiMerged");
      }
      if (!this.showMergeFlowsButton || !this.showMergeUiButton) {
        this.updateBranch();
      }
    },
    /*
     * Update the merged branch
     */
    updateBranch() {
      this.showMergeFlowsPage = false;
      this.showMergeUiPage = false;
      this.buttonResult = "";
      this.loadingButton = true;
      this.errmsg = "success";
      this.axios
        .post("/api/updatebranch", {
          branch: this.branchInfo.branchName,
          nodes: this.mergedNodes,
          html: this.html,
          js: this.js,
          css: this.css,
        })
        .then((response) => {
          var f = response.data;
          if (String(f).startsWith("svn")) {
//            console.log(f);
            this.buttonResult = f;
            this.errmsg = "danger";
            this.loadingButton = false;
          } else {
            this.buttonResult = f;
            this.errmsg = "success";
            this.loadingButton = false;
            this.retrieveResult = "";
          }
        })
        .catch((error) => {
//          console.log(error.message);
          this.retrieveResult = error.message;
          this.errmsg = "danger";
          this.loadingButton = false;
        });
    },
    flowsAreMerged(flowsMerged) {
//      console.log("flowsAreMerged called");
      this.flowsMerged = flowsMerged;
      if (this.flowsMerged) {
        this.emitter.emit("GetChangedNodes");
      } else {
        this.errmsg = "danger";
        this.buttonResult = "Not all nodes are merged yet";
      }
    },
    uiIsMerged(uiMerged) {
//      console.log("uiIsMerged called");
      if (uiMerged) {
        this.emitter.emit("GetChangedHtml");
      } else {
        this.errmsg = "danger";
        this.buttonResult += (this.buttonResult == "" ? "N" : " and n") + "ot all UI files are merged.";
      }
    },
  },
  watch: {
    flowsMerged() {
//      console.log("flowsMerged changed");
      this.allMerged = this.flowsMerged && this.htmlMerged && this.jsMerged && this.cssMerged;
    },
    htmlMerged() {
//      console.log("htmlMerged changed");
      this.allMerged = this.flowsMerged && this.htmlMerged && this.jsMerged && this.cssMerged;
      this.emitter.emit("GetChangedJs");

    },
    jsMerged() {
//      console.log("jsMerged changed");
      this.allMerged = this.flowsMerged && this.htmlMerged && this.jsMerged && this.cssMerged;
      this.emitter.emit("GetChangedCss");
    },
    cssMerged() {
//      console.log("cssMerged changed");
      this.allMerged = this.flowsMerged && this.htmlMerged && this.jsMerged && this.cssMerged;
    },
    allMerged(val) {
//      console.log("allMerged changed");
      if (val) {
        this.updateBranch();
      }
    }
  },
  mounted() {
    this.emitter.on("RefreshHome", () => {
      this.getCurrentBranch();
    });
    this.getCurrentBranch();
    this.emitter.on("FlowsAreTheSame", (same) => {
      console.log("FlowsAreTheSame request: ", same);
      this.showMergeFlowsButton = !same;
    });
    this.emitter.on("FlowsMerged", (flowsMerged) => {
      console.log("FlowsMerged request: ", flowsMerged);
      this.flowsAreMerged(flowsMerged);
    });
    this.emitter.on("ChangedNodes", (nodes) => {
      console.log("ChangedNodes request");
      for (var a of nodes.added) {
        if (a.add) {
          this.mergedNodes.push({
            nodeId: a.nodeId,
            nodeContent: a.nodeContent,
          });
        }
      }
      for (var n of nodes.changed) {
        this.mergedNodes.push({ nodeId: n.nodeId, nodeContent: n.nodeContent });
      }
      this.flowsMerged = true;
      this.emitter.emit("IsUiMerged");
    });
    this.emitter.on("UiIsTheSame", (same) => {
      console.log("UiIsTheSame request", same);
      this.showMergeUiButton = !same;
    });
    this.emitter.on("UiMerged", (uiMerged) => {
      console.log("UiMerged request", uiMerged);
      this.uiIsMerged(uiMerged);
    });
    this.emitter.on("ChangedHtml", (changedHtml) => {
      console.log("ChangedHtml request");
      this.html = changedHtml;
      this.htmlMerged = true; 
    });
    this.emitter.on("ChangedJs", (changedJs) => {
      console.log("ChangedJs request");
      this.js = changedJs;
      this.jsMerged = true; 
    });
    this.emitter.on("ChangedCss", (changedCss) => {
      console.log("ChangedCss request");
      this.css = changedCss;
      this.cssMerged = true; 
    });
  },
}
</script>
<style>
</style>