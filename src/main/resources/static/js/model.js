let model = {
  rows: [
  ],
  meta: {
  }
};

let modelChanges = {
  changes: [
    /*
      main concepts:
      colId: A,B,C,...AA,...
      rowId: 111.111.111.111 |  <prices>.123  (here <prices> is for 111.111.111)
      cellId: AXY111.111.111.111
      cellIdRange: ABC111:XYZ999.777
    */

    /*
      Functions:
      - aggregate type
        - SUM(<range>) | SUM, AVG, COUNT
      
    */
  
    /*
      list of operations (see GDocs)
    */
  
    /* t(type): 
          0 - noop,
          111 - add column(s) after {colId}, 112 - add column(s) before {colId}, 113 - add column(s) as a first column
                  cid:colId
                  cc:columnsCount
                  styling: {w:width,bc:bgcolor,lbw:leftBorderWidth,;lbc:leftBorderColor,lbt:leftBorderType,rbw:rightBorderWidth,rbc,rbt}
                  cellsData: data with cells
          
          121 - delete column(s) [{colId}]
          
          131 - resize column(s) [{colId}]
          132 - hide column(s) [{colId}]
          133 - show column(s) [{colId}]
          
          141 - modify cells in column {colId}
          
          211 - add row(s) after {rowId}, 212 - add row(s) before {rowId}, 213 - add row(s) as a first row(s)
              // this is only for rows on same level
              
          221 - add row as first child of {parentRowId}
    */
    {
      tmstmp: 347893798734,
      ops: [
        { t: 113, cc: 2, w: 55, bc:'#1155ff', lb:2,lbc:'red', cellsData: [""]},
        { }
      ]
    },
  ]
}