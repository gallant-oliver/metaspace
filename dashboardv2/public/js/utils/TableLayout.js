/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @file This is the common View file for displaying Table/Grid to be used overall in the application.
 */
define(['require',
    'backbone',
    'hbs!tmpl/common/TableLayout_tmpl',
    'backgrid-filter',
    'backgrid-paginator',
    'backgrid-sizeable',
    'backgrid-orderable',
    'backgrid-select-all',
    'backgrid-columnmanager'
], function(require, Backbone, FSTablelayoutTmpl) {
    'use strict';

    var FSTableLayout = Backbone.Marionette.LayoutView.extend(
        /** @lends FSTableLayout */
        {
            _viewName: 'FSTableLayout',

            template: FSTablelayoutTmpl,
            templateHelpers: function() {
                return this.options;
            },

            /** Layout sub regions */
            regions: {
                'rTableList': 'div[data-id="r_tableList"]',
                'rTableSpinner': 'div[data-id="r_tableSpinner"]',
                'rPagination': 'div[data-id="r_pagination"]',
                'rFooterRecords': 'div[data-id="r_footerRecords"]'
            },

            // /** ui selector cache */
            ui: {
                selectPageSize: 'select[data-id="pageSize"]'
            },

            gridOpts: {
                className: 'table table-bordered table-hover table-condensed backgrid',
                emptyText: 'No Records found!'
            },

            /**
             * Backgrid.Filter default options
             */
            filterOpts: {
                placeholder: 'plcHldr.searchByResourcePath',
                wait: 150
            },

            /**
             * Paginator default options
             */
            paginatorOpts: {
                // If you anticipate a large number of pages, you can adjust
                // the number of page handles to show. The sliding window
                // will automatically show the next set of page handles when
                // you click next at the end of a window.
                windowSize: 5, // Default is 10

                // Used to multiple windowSize to yield a number of pages to slide,
                // in the case the number is 5
                slideScale: 0.5, // Default is 0.5

                // Whether sorting should go back to the first page
                goBackFirstOnSort: false // Default is true
            },

            /**
               page handlers for pagination
            */
            controlOpts: {
                rewind: null,
                back: {
                    label: "<i class='fa fa-angle-left'></i>",
                    title: "Previous"
                },
                forward: {
                    label: "<i class='fa fa-angle-right'></i>",
                    title: "Next"
                },
                fastForward: null
            },
            columnOpts: {
                opts: {
                    initialColumnsVisible: 4,
                    // State settings
                    saveState: false,
                    loadStateOnInit: true
                },
                visibilityControlOpts: {},
                el: null
            },

            includePagination: true,

            includeFilter: false,

            includeHeaderSearch: false,

            includePageSize: false,

            includeFooterRecords: true,

            includeColumnManager: false,

            includeSizeAbleColumns: false,

            includeOrderAbleColumns: false,

            includeTableLoader: true,


            /** ui events hash */
            events: function() {
                var events = {};
                events['change ' + this.ui.selectPageSize] = 'onPageSizeChange';
                return events;
            },

            /**
             * intialize a new HDFSTableLayout Layout
             * @constructs
             */
            initialize: function(options) {
                _.extend(this, _.pick(options, 'collection', 'columns', 'includePagination',
                    'includeHeaderSearch', 'includeFilter', 'includePageSize',
                    'includeFooterRecords', 'includeColumnManager', 'includeSizeAbleColumns', 'includeOrderAbleColumns', 'includeTableLoader'));

                _.extend(this.gridOpts, options.gridOpts, { collection: this.collection, columns: this.columns });
                _.extend(this.filterOpts, options.filterOpts);
                _.extend(this.paginatorOpts, options.paginatorOpts);
                _.extend(this.controlOpts, options.controlOpts);
                _.extend(this.columnOpts, options.columnOpts);

                this.bindEvents();
            },

            /** all events binding here */
            bindEvents: function() {
                this.listenTo(this.collection, 'request', function() {
                    this.$('div[data-id="r_tableSpinner"]').addClass('show');
                }, this);
                this.listenTo(this.collection, 'sync error', function() {
                    this.$('div[data-id="r_tableSpinner"]').removeClass('show');
                }, this);

                this.listenTo(this.collection, 'reset', function(collection, response) {
                    if (this.includePagination) {
                        this.renderPagination();
                    }
                    if (this.includeFooterRecords) {
                        this.renderFooterRecords(this.collection.state);
                    }
                }, this);

                /*This "sort" trigger event is fired when clicked on
                'sortable' header cell (backgrid).
                Collection.trigger event was fired because backgrid has
                removeCellDirection function (backgrid.js - line no: 2088)
                which is invoked when "sort" event is triggered
                on collection (backgrid.js - line no: 2081).
                removeCellDirection function - removes "ascending" and "descending"
                which in turn removes chevrons from every 'sortable' header-cells*/
                this.listenTo(this.collection, "backgrid:sort", function() {
                    this.collection.trigger("sort");
                });

                // It will show tool tip when td has ellipsis  Property
                this.listenTo(this.collection, "backgrid:refresh", function() {
                    /*this.$('.table td').bind('mouseenter', function() {
                        var $this = $(this);
                        if (this.offsetWidth < this.scrollWidth && !$this.attr('title')) {
                            $this.attr('title', $this.text());
                        }
                    });*/
                }, this);

            },

            /** on render callback */
            onRender: function() {
                this.renderTable();
                if (this.includePagination) {
                    this.renderPagination();
                }
                if (this.includeFilter) {
                    this.renderFilter();
                }
                if (this.includeFooterRecords) {
                    this.renderFooterRecords(this.collection.state);
                }
                if (this.includeColumnManager) {
                    this.renderColumnManager();
                }
                if (this.includeSizeAbleColumns) {
                    this.renderSizeAbleColumns();
                }
                if (this.includeOrderAbleColumns) {
                    this.renderOrderAbleColumns();
                }
                if (this.includePageSize) {
                    this.ui.selectPageSize.select2({
                        data: _.sortBy(_.union([25, 50, 100, 150, 200, 250, 300, 350, 400, 450, 500], [this.collection.state.pageSize])),
                        tags: true,
                        dropdownCssClass: "number-input",
                        multiple: false
                    });
                    this.ui.selectPageSize.val(this.collection.state.pageSize).trigger('change', { "skipViewChange": true });
                }

            },
            /**
             * show table
             */
            renderTable: function() {
                var that = this;
                this.rTableList.show(new Backgrid.Grid(this.gridOpts).on('backgrid:rendered', function() {
                    that.trigger('backgrid:rendered', this)
                }));
            },

            /**
             * show pagination buttons(first, last, next, prev and numbers)
             */
            renderPagination: function() {
                var options = _.extend({
                    collection: this.collection,
                    controls: this.controlOpts
                }, this.paginatorOpts);

                // TODO - Debug this part
                if (this.rPagination) {
                    this.rPagination.show(new Backgrid.Extension.Paginator(options));
                } else if (this.regions.rPagination) {
                    this.$('div[data-id="r_pagination"]').show(new Backgrid.Extension.Paginator(options));
                }
            },

            /**
             * show/hide pagination buttons of the grid
             */
            showHidePager: function() {

                if (!this.includePagination) {
                    return;
                }

                if (this.collection.state && this.collection.state.totalRecords > this.collection.state.pageSize) {
                    this.$('div[data-id="r_pagination"]').show();
                } else {
                    this.$('div[data-id="r_pagination"]').hide();
                }
            },

            /**
             * show/hide filter of the grid
             */
            renderFilter: function() {
                this.rFilter.show(new Backgrid.Extension.ServerSideFilter({
                    collection: this.collection,
                    name: ['name'],
                    placeholder: 'plcHldr.searchByResourcePath',
                    wait: 150
                }));

                setTimeout(function() {
                    that.$('table').colResizable({ liveDrag: true });
                }, 0);
            },

            /**
             * show/hide footer details of the list/collection shown in the grid
             */
            renderFooterRecords: function(collectionState) {
                var collState = collectionState;
                var totalRecords = collState.totalRecords || 0;
                var pageStartIndex = totalRecords ? (collState.currentPage * collState.pageSize) : 0;
                var pageEndIndex = pageStartIndex + this.collection.length;
                this.$('[data-id="r_footerRecords"]').html('<h5>Showing ' + (totalRecords ? pageStartIndex + 1 : (this.collection.length === 0) ? 0 : 1) + ' - ' + pageEndIndex + '</h5>');
                return this;
            },
            /**
             * ColumnManager for the table
             */
            renderColumnManager: function() {
                var that = this,
                    $el = this.columnOpts.el || this.$("[data-id='control']"),
                    colManager = new Backgrid.Extension.ColumnManager(this.columns, this.columnOpts.opts),
                    // Add control
                    colVisibilityControl = new Backgrid.Extension.ColumnManagerVisibilityControl(_.extend({
                        columnManager: colManager,
                    }, this.columnOpts.visibilityControlOpts));
                if (!$el.jquery) {
                    $el = $($el)
                }
                if (this.columnOpts.el) {
                    $el.html(colVisibilityControl.render().el);
                } else {
                    $el.append(colVisibilityControl.render().el);
                }

                colManager.on("state-changed", function(state) {
                    that.collection.trigger("state-changed", state);
                });
                colManager.on("state-saved", function() {
                    that.collection.trigger("state-changed");
                });
            },

            renderSizeAbleColumns: function() {
                // Add sizeable columns
                var sizeAbleCol = new Backgrid.Extension.SizeAbleColumns({
                    collection: this.collection,
                    columns: this.columns,
                    grid: this.getGridObj()
                });
                this.$('thead').before(sizeAbleCol.render().el);

                // Add resize handlers
                var sizeHandler = new Backgrid.Extension.SizeAbleColumnsHandlers({
                    sizeAbleColumns: sizeAbleCol,
                    // grid: this.getGridObj(),
                    saveModelWidth: true
                });
                this.$('thead').before(sizeHandler.render().el);

                // Listen to resize events
                this.columns.on('resize', function(columnModel, newWidth, oldWidth) {
                    console.log('Resize event on column; name, model, new and old width: ', columnModel.get("name"), columnModel, newWidth, oldWidth);
                });
            },

            renderOrderAbleColumns: function() {
                // Add orderable columns
                var sizeAbleCol = new Backgrid.Extension.SizeAbleColumns({
                    collection: this.collection,
                    grid: this.getGridObj(),
                    columns: this.columns
                });
                this.$('thead').before(sizeAbleCol.render().el);
                var orderHandler = new Backgrid.Extension.OrderableColumns({
                    grid: this.getGridObj(),
                    sizeAbleColumns: sizeAbleCol
                });
                this.$('thead').before(orderHandler.render().el);
            },

            /** on close */
            onClose: function() {},

            /**
             * get the Backgrid object
             * @return {null}
             */
            getGridObj: function() {
                if (this.rTableList.currentView) {
                    return this.rTableList.currentView;
                }
                return null;
            },

            /**
             * handle change event on page size select box
             * @param  {Object} e event
             */
            onPageSizeChange: function(e, options) {
                if (!options || !options.skipViewChange) {
                    var pagesize = $(e.currentTarget).val();
                    this.collection.state.pageSize = parseInt(pagesize, 10);

                    this.collection.state.currentPage = this.collection.state.firstPage;
                    if (this.collection.mode == "client") {
                        this.collection.reset(this.collection.toJSON());
                    } else {
                        this.collection.fetch({
                            sort: false,
                            reset: true,
                            cache: false
                        });
                    }
                }
            }
        });

    return FSTableLayout;
});