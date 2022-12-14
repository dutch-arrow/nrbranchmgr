<template>
  <b-card title="Update Branch" v-show="showPage">
    <b-alert v-show="retrieveResult !== ''" :variant="errmsg" show>{{ retrieveResult }}</b-alert>
    <b-overlay :show="loading" variant="light" opacity="0.6" rounded="sm"></b-overlay>
  </b-card>
</template>

<script>
export default {
  name: "UpdateBranch",
  props: [
    "showPage",
  ],
  data() {
    return {
      flowsMerged: false,
      htmlMerged: false,
      jsMerged: false,
      cssMerged: false,
      allMerged: false,
      changedNodes: {},
      changedHtml: {},
      changedJs: {},
      changedCss: {},
      loading: false,
      errmsg: "",
      retrieveResult: "",
    };
  },
  methods: {
    /*
     * Update the merged branch
     */
    updateBranch() {
      this.retrieveResult = "";
      this.errmsg = "success";
      this.axios
        .post("/api/updatebranch", {
          branch: this.branchName,
          nodes: this.mergedNodes,
          html: this.html,
          js: this.js,
          css: this.css,
        })
        .then((response) => {
          var f = response.data;
          if (String(f).startsWith("svn")) {
//            console.log(f);
            this.retrieveResult = f;
            this.errmsg = "danger";
            this.loading = false;
          } else {
            this.retrieveResult = f;
            this.errmsg = "success";
            this.loading = false;
            
          }
        })
        .catch((error) => {
//          console.log(error.message);
          this.retrieveResult = error.message;
          this.errmsg = "danger";
          this.loading = false;
        });
    },
    flowsAreMerged(flowsMerged) {
//      console.log("flowsAreMerged called");
      this.flowsMerged = flowsMerged;
      if (this.flowsMerged) {
        this.emitter.emit("GetChangedNodes");
      } else {
        this.errmsg = "danger";
        this.retrieveResult += "Not all nodes are merged yet";
      }
    },
    uiIsMerged(uiMerged) {
//      console.log("uiIsMerged called");
      if (uiMerged) {
        this.emitter.emit("GetChangedHtml");
      } else {
        this.errmsg = "danger";
        this.retrieveResult += (this.retrieveResult == "" ? "N" : " and n") + "ot all UI files are merged.";
      }
    }
  },
  watch: {
    showPage(val) {
//      console.log("showPage changed");
      if (val) {
        this.loading = true;
        this.retrieveResult = "";
        this.flowsMerged = false;
        this.mergedNodes = [];
        this.emitter.emit("AreFlowsMerged");
      }
    },
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
    this.loading = true;
    this.emitter.on("FlowsMerged", (flowsMerged) => {
      this.flowsAreMerged(flowsMerged);
    });
    this.emitter.on("ChangedNodes", (nodes) => {
//      console.log("ChangedNodes request");
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
    this.emitter.on("UiMerged", (uiMerged) => {
      this.uiIsMerged(uiMerged);
    });
    this.emitter.on("ChangedHtml", (changedHtml) => {
//      console.log("ChangedHtml request");
      this.html = changedHtml;
      this.htmlMerged = true; 
    });
    this.emitter.on("ChangedJs", (changedJs) => {
//      console.log("ChangedJs request");
      this.js = changedJs;
      this.jsMerged = true; 
    });
    this.emitter.on("ChangedCss", (changedCss) => {
//      console.log("ChangedCss request");
      this.css = changedCss;
      this.cssMerged = true; 
    });
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped></style>
