/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
require([ "jquery", "$!services.webjars.url('jstree', 'jstree.js')" ], function($) {
  // Create the tree and open all nodes when ready
  $('#extension_clean_tree')
    .on("changed.jstree", function(e, data) {
      var uninstalledExtensions = $('#uninstalled_extensions');
      uninstalledExtensions.empty();
      data.selected.forEach(function(e) {
        uninstalledExtensions.append($('<input>').attr({
          type: 'hidden',
          name: 'uninstalled_extensions',
          value: e
        }));        
      });
    })
    .jstree({
      "core" : {
        "themes" : {
          "icons" : false
        }
      },
      "checkbox" : {
        "three_state" : false,
        "cascade" : "up+undetermined"
      },
      "plugins" : [ "checkbox" ]
    });


  var maybeScheduleRefresh = function(jobStatus) {
    // Refresh if the job is running (if the progress bar is displayed).
    if (jobStatus.children('.ui-progress').size() > 0) {
      setTimeout(maybeScheduleRefresh, 500, jobStatus);
    } else {
      // Re-enable the buttons.
      jobStatus.parent('form').find('button').prop('disabled', false);
    }
  };

  // Automatically enable any button associated with a running job
  $('.job-status').has('.ui-progress').each(function() {
    maybeScheduleRefresh($(this));
  });
});
