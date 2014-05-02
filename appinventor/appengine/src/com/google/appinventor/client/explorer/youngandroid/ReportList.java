// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;


import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppReport;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.user.User;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;








/**
 * The report list shows all reports in a table.
 *
 * <p> The report text, date created, user reported on and user reporting will be shown in the table.
 *
 * @author wolberd@gmail.com, based on ProjectList.java, lizlooney@google.com (Liz Looney),
 */
public class ReportList extends Composite  {
  private final CheckBox checkBox;
  private final VerticalPanel panel;
  private List<GalleryAppReport> reports;
  private List<GalleryAppReport> selectedReports;
  private final List<GalleryAppReport> selectedGalleryAppReports;
  private final Map<GalleryAppReport, ReportWidgets> ReportWidgets;
  private DropDownButton templateButton;

  // UI elements
  private final Grid table;

  /**
   * Creates a new ProjectList
   */
  public ReportList() {



    // Initialize UI
    panel = new VerticalPanel();
    panel.setWidth("100%");

    HorizontalPanel checkBoxPanel = new HorizontalPanel();
    checkBoxPanel.addStyleName("all-reports");
    checkBox = new CheckBox();
    checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
        if (isChecked) {
          initializeAllReports();
        } else {
          initializeReports();
        }
    }
    });
    checkBoxPanel.add(checkBox);
    Label checkBoxText = new Label("Show Resolved Reports");
    checkBoxPanel.add(checkBoxText);
    panel.add(checkBoxPanel);

    selectedGalleryAppReports = new ArrayList<GalleryAppReport>();
    ReportWidgets = new HashMap<GalleryAppReport, ReportWidgets>();

    table = new Grid(1, 8); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);

    setHeaderRow();

    panel.add(table);
    initWidget(panel);

    initializeReports();

  }

  /**
   * Adds the header row to the table.
   *
   */
  private void setHeaderRow() {
    table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");

    HorizontalPanel reportHeader = new HorizontalPanel();
    final Label reportHeaderLabel = new Label(MESSAGES.moderationReportTextHeader());
    reportHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    reportHeader.add(reportHeaderLabel);
    table.setWidget(0, 0, reportHeader);

    HorizontalPanel appHeader = new HorizontalPanel();
    final Label appHeaderLabel = new Label(MESSAGES.moderationAppHeader());
    appHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    appHeader.add(appHeaderLabel);
    table.setWidget(0, 1, appHeader);

    HorizontalPanel dateCreatedHeader = new HorizontalPanel();
    final Label dateCreatedHeaderLabel = new Label(MESSAGES.moderationReportDateCreatedHeader());
    dateCreatedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedHeaderLabel);
    table.setWidget(0, 2, dateCreatedHeader);

    HorizontalPanel appAuthorHeader = new HorizontalPanel();
    final Label appAuthorHeaderLabel = new Label(MESSAGES.moderationAppAuthorHeader());
    appAuthorHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    appAuthorHeader.add(appAuthorHeaderLabel);
    table.setWidget(0, 3, appAuthorHeader);

    HorizontalPanel reporterHeader = new HorizontalPanel();
    final Label reporterHeaderLabel = new Label(MESSAGES.moderationReporterHeader());
    reporterHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    reporterHeader.add(reporterHeaderLabel);
    table.setWidget(0, 4, reporterHeader);

  }

  private void initializeReports() {
    final OdeAsyncCallback<List<GalleryAppReport>> callback = new OdeAsyncCallback<List<GalleryAppReport>>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(List<GalleryAppReport> reportList) {
                reports=reportList;
                ReportWidgets.clear();
                for (GalleryAppReport report : reports) {
                  ReportWidgets.put(report, new ReportWidgets(report));
                }
                refreshTable();
              }
          };
        Ode.getInstance().getGalleryService().getRecentReports(0,10,callback);
  }

  private void initializeAllReports() {
    final OdeAsyncCallback<List<GalleryAppReport>> callback = new OdeAsyncCallback<List<GalleryAppReport>>(
      // failure message
      MESSAGES.galleryError()) {
        @Override
        public void onSuccess(List<GalleryAppReport> reportList) {
          reports=reportList;
          ReportWidgets.clear();
          for (GalleryAppReport report : reports) {
            ReportWidgets.put(report, new ReportWidgets(report));
          }
          refreshTable();
        }
      };
      Ode.getInstance().getGalleryService().getAllAppReports(0,10,callback);
  }

  private class ReportWidgets {
    final Label reportTextLabel;
    final Label appLabel;
    final Label dateCreatedLabel;
    final Label appAuthorlabel;
    final Label reporterLabel;
    final Button sendMessageButton;
    final Button deactiveAppButton;
    final Button markAsResolvedButton;
    boolean appActive;
    boolean appResolved;

    private ReportWidgets(final GalleryAppReport report) {

      reportTextLabel = new Label(report.getReportText());
      reportTextLabel.addStyleName("ode-ProjectNameLabel");

      appLabel = new Label(report.getApp().getTitle());
      appLabel.addStyleName("ode-ProjectNameLabel");

      DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
      Date dateCreated = new Date(report.getTimeStamp());
      dateCreatedLabel = new Label(dateTimeFormat.format(dateCreated));

      appAuthorlabel = new Label(report.getOffender().getUserName());
      appAuthorlabel.addStyleName("ode-ProjectNameLabel");

      reporterLabel = new Label(report.getReporter().getUserName());
      reporterLabel.addStyleName("ode-ProjectNameLabel");

      sendMessageButton = new Button("Send Message");

      deactiveAppButton = new Button("Deactive App");

      markAsResolvedButton = new Button("Mark As Resolved");


    }
  }

  private void refreshTable() {

    // Refill the table.
    table.resize(1 + reports.size(), 8);
    int row = 1;
    for (GalleryAppReport report : reports) {
      ReportWidgets rw = ReportWidgets.get(report);
      table.setWidget(row, 0, rw.reportTextLabel);
      table.setWidget(row, 1, rw.appLabel);
      table.setWidget(row, 2, rw.dateCreatedLabel);
      table.setWidget(row, 3, rw.appAuthorlabel);
      table.setWidget(row, 4, rw.reporterLabel);
      table.setWidget(row, 5, rw.sendMessageButton);
      table.setWidget(row, 6, rw.deactiveAppButton);
      table.setWidget(row, 7, rw.markAsResolvedButton);
      prepareGalleryAppReport(report, rw);
      row++;
    }

    Ode.getInstance().getProjectToolbar().updateButtons();
  }

  /**
   *
   */
  private void prepareGalleryAppReport(final GalleryAppReport r, final ReportWidgets rw) {
    rw.reportTextLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

      }
    });

    rw.appLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(r.getApp(), GalleryPage.VIEWAPP);
        }
    });

    rw.appAuthorlabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToUserProfileView(r.getOffender().getUserId(), 1 /* 1 for public view*/ );
        }
    });

    rw.reporterLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            Ode.getInstance().switchToUserProfileView(r.getReporter().getUserId(), 1 /* 1 for public view*/ );
        }
    });

    rw.sendMessageButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        sendMessagePopup(r);
      }
    });

    final OdeAsyncCallback<Boolean> isActivatedCallback = new OdeAsyncCallback<Boolean>(
    // failure message
    MESSAGES.galleryError()) {
      @Override
      public void onSuccess(Boolean active) {
        if(active){
          rw.deactiveAppButton.setText("Deactivate App");
          rw.appActive = true;
        }
        else {
          rw.deactiveAppButton.setText("Reactivate App");
          rw.appActive = false;
        }
      }
    };
    Ode.getInstance().getGalleryService().isGalleryAppActivated(r.getApp().getGalleryAppId(), isActivatedCallback);

    rw.deactiveAppButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          deactiveAppPopup(r, rw);
        }
    });

    if(r.getResolved()){//current status is resolved
      rw.markAsResolvedButton.setText("Mark As Unresolved");//revert button
      rw.appResolved = true;
    }else{//current status is unresolved
      rw.markAsResolvedButton.setText("Mark As Resolved");//revert button
      rw.appResolved = false;
    }
    OdeLog.log("######### Setup markReportAsResolved: r.getReportId():" + r.getReportId() + ", r.getReportText():" + r.getReportText());
    rw.markAsResolvedButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Boolean> callback = new OdeAsyncCallback<Boolean>(
          // failure message
          MESSAGES.galleryError()) {
            @Override
            public void onSuccess(Boolean success) {
              if(success){
                if(checkBox.getValue() == false){//only unresolved reports, remove directly.
                  onReportRemoved(r);
                }else{//both resolved and unresolved reports
                  if(r.getResolved()){//current status is resolved
                    r.setResolved(false);
                    rw.markAsResolvedButton.setText("Mark As Resolved");//revert button
                    rw.appResolved = false;
                  }else{//current status is unResolved
                    r.setResolved(true);
                    rw.markAsResolvedButton.setText("Mark As UnResolved");//revert button
                    rw.appResolved = true;
                  }
                }
              }
            }
          };
        Ode.getInstance().getGalleryService().markReportAsResolved(r.getReportId(), callback);
      }
    });
  }

  /**
   * Gets the number of reports
   *
   * @return the number of reports
   */
  public int getNumGalleryAppReports() {
    return reports.size();
  }

  /**
   * Gets the number of selected reports
   *
   * @return the number of selected reports
   */
  public int getNumSelectedGalleryAppReports() {
    return selectedGalleryAppReports.size();
  }

  /**
   * Returns the list of selected reports
   *
   * @return the selected reports
   */
  public List<GalleryAppReport> getSelectedGalleryAppReports() {
    return selectedGalleryAppReports;
  }

  public void onReportAdded(GalleryAppReport report) {
    reports.add(report);
    ReportWidgets.put(report, new ReportWidgets(report));
    refreshTable();
  }

  public void onReportRemoved(GalleryAppReport report) {
    reports.remove(report);
    ReportWidgets.remove(report);

    refreshTable();

    selectedGalleryAppReports.remove(report);
  }

  private void sendMessagePopup(final GalleryAppReport report){
      // Create a PopUpPanel with a button to close it
      final PopupPanel popup = new PopupPanel(true);
      popup.setStyleName("ode-InboxContainer");
      final FlowPanel content = new FlowPanel();
      content.addStyleName("ode-Inbox");
      Label title = new Label(MESSAGES.messageInboxTitle());
      title.addStyleName("InboxTitle");
      content.add(title);

      Button closeButton = new Button("x");
//      closeButton.addStyleName("ActionButton");
      closeButton.addStyleName("CloseButton");
      closeButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          popup.hide();
        }
      });
      content.add(closeButton);

      final FlowPanel msgPanel = new FlowPanel();
      msgPanel.addStyleName("app-actions");
      final Label sentFrom = new Label("Sent From: ");
      final Label sentTo = new Label("Sent To: " + report.getOffender().getUserName());
      final TextArea msgText = new TextArea();
      msgText.addStyleName("action-textarea");
      final Button sendMsg = new Button("Send Message");
      sendMsg.addStyleName("action-button");

      // Account Drop Down Button
      List<DropDownItem> templateItems = Lists.newArrayList();
      // Messages Template 1
      templateItems.add(new DropDownItem("template1", "Inappropriate App Content", new TemplateAction(msgText, 1, report.getApp().getTitle())));
      templateItems.add(new DropDownItem("template2", "Inappropriate User profile content", new TemplateAction(msgText, 2, null)));
      templateButton = new DropDownButton("template", "Choose Template" , templateItems, true);
      templateButton.setStyleName("ode-TopPanelButton");

      new TemplateAction(msgText, 1, report.getApp().getTitle()).execute();

      msgPanel.add(templateButton);
      msgPanel.add(sentFrom);
      msgPanel.add(sentTo);
      msgPanel.add(msgText);
      msgPanel.add(sendMsg);

      content.add(msgPanel);
      popup.setWidget(content);
      // Center and show the popup
      popup.center();

      OdeAsyncCallback<User> callback = new OdeAsyncCallback<User>(
        // failure message
        MESSAGES.serverUnavailable()) {
          @Override
          public void onSuccess(final User currentUser) {
            sentFrom.setText("Sent From: " + currentUser.getUserName());
            sendMsg.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                final OdeAsyncCallback<Void> messagesCallback = new OdeAsyncCallback<Void>(
                  MESSAGES.galleryError()) {
                    @Override
                    public void onSuccess(final Void result) {
                      OdeLog.log("### Moderator MSGS SEND SUCCESSFULLY");
                      popup.hide();
                    }
                  };
                  Ode.getInstance().getGalleryService().sendMessageFromSystem(currentUser.getUserId(), report.getOffender().getUserId(), msgText.getText(), messagesCallback);
              }
            });
          }
        };
      Ode.getInstance().getUserInfoService().getUserInformation(callback);
    }
  private void deactiveAppPopup(final GalleryAppReport r, final ReportWidgets rw){
      // Create a PopUpPanel with a button to close it
      final PopupPanel popup = new PopupPanel(true);
      popup.setStyleName("ode-InboxContainer");
      final FlowPanel content = new FlowPanel();
      content.addStyleName("ode-Inbox");
      Label title = new Label(MESSAGES.messageInboxTitle());
      title.addStyleName("InboxTitle");
      content.add(title);

      Button closeButton = new Button("x");
//      closeButton.addStyleName("ActionButton");
      closeButton.addStyleName("CloseButton");
      closeButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          popup.hide();
        }
      });
      content.add(closeButton);

      final FlowPanel msgPanel = new FlowPanel();
      msgPanel.addStyleName("app-actions");
      final Label sentFrom = new Label("Sent From: ");
      final Label sentTo = new Label("Sent To: " + r.getOffender().getUserName());
      final TextArea msgText = new TextArea();
      msgText.addStyleName("action-textarea");
      final Button sendMsgAndDRApp = new Button("Send Message & ");
      sendMsgAndDRApp.addStyleName("action-button");
      final Button cancel = new Button("Cancel");
      cancel.addStyleName("action-button");

      // Account Drop Down Button
      List<DropDownItem> templateItems = Lists.newArrayList();
      // Messages Template 1
      templateItems.add(new DropDownItem("template1", "Inappropriate App Content", new TemplateAction(msgText, 1, r.getApp().getTitle())));
      templateItems.add(new DropDownItem("template2", "Inappropriate User profile content", new TemplateAction(msgText, 2, null)));
      templateButton = new DropDownButton("template", "Choose Template" , templateItems, true);
      templateButton.setStyleName("ode-TopPanelButton");

      new TemplateAction(msgText, 1, r.getApp().getTitle()).execute();

      msgPanel.add(templateButton);
      msgPanel.add(sentFrom);
      msgPanel.add(sentTo);
      msgPanel.add(msgText);
      msgPanel.add(sendMsgAndDRApp);
      msgPanel.add(cancel);

      content.add(msgPanel);
      popup.setWidget(content);
      // Center and show the popup
      popup.center();

      cancel.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            popup.hide();
          }
      });

      final OdeAsyncCallback<Boolean> isActivatedCallback = new OdeAsyncCallback<Boolean>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Boolean active) {
            if(active){
              sendMsgAndDRApp.setText("Send Message & Reactivate App");
             }
            else {
              sendMsgAndDRApp.setText("Send Message & Deactivate App");
            }
          }
      };
      Ode.getInstance().getGalleryService().isGalleryAppActivated(r.getApp().getGalleryAppId(), isActivatedCallback);

      OdeAsyncCallback<User> callback = new OdeAsyncCallback<User>(
        // failure message
        MESSAGES.serverUnavailable()) {
          @Override
          public void onSuccess(final User currentUser) {
            sentFrom.setText("Sent From: " + currentUser.getUserName());
            sendMsgAndDRApp.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                final OdeAsyncCallback<Void> messagesCallback = new OdeAsyncCallback<Void>(
                  MESSAGES.galleryError()) {
                    @Override
                    public void onSuccess(final Void result) {
                      OdeLog.log("### Moderator MSGS SEND SUCCESSFULLY");
                      popup.hide();

                      final OdeAsyncCallback<Boolean> callback = new OdeAsyncCallback<Boolean>(
                        // failure message
                        MESSAGES.galleryError()) {
                          @Override
                            public void onSuccess(Boolean success) {
                              if(!success)
                                return;
                              OdeLog.log("### Moderator APP DEACTIVATED/REACTIVED SUCCESSFULLY");
                              popup.hide();
                              if(rw.appActive == true){
                                rw.deactiveAppButton.setText("Reactivate App");//revert button
                                rw.appActive = false;
                                sendMsgAndDRApp.setText("Send Message & Reactivate App");
                              }else{
                                rw.deactiveAppButton.setText("Deactivate App");//revert button
                                rw.appActive = true;
                                sendMsgAndDRApp.setText("Send Message & Deactivate App");
                              }
                            }
                         };
                      Ode.getInstance().getGalleryService().deactivateGalleryApp(r.getApp().getGalleryAppId(), callback);
                    }
                  };
                  Ode.getInstance().getGalleryService().sendMessageFromSystem(currentUser.getUserId(), r.getOffender().getUserId(), msgText.getText(), messagesCallback);
              }
            });
          }
        };
      Ode.getInstance().getUserInfoService().getUserInformation(callback);
    }
  /**
   *
   * this is a template using for update database.
   * update Database Field, should only be used by system admin
   */
  private void setUpdateDatabaseButton(){
    /*
	final Button button = new Button("Update Database Field");
    button.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          OdeAsyncCallback<Void> updateCallback = new OdeAsyncCallback<Void>(
          // failure message
          MESSAGES.serverUnavailable()) {
            @Override
            public void onSuccess(final Void callBack) {
              button.setVisible(true);
            }
          };
          Ode.getInstance().getGalleryService().updateDatabaseField(updateCallback);
      }
    });
    panel.add(button);
    */
    final Button button = new Button("Update Database Field");
    button.setVisible(false);

    OdeAsyncCallback<User> callback = new OdeAsyncCallback<User>(
	// failure message
    MESSAGES.serverUnavailable()) {
      @Override
      public void onSuccess(final User currentUser) {
        if(currentUser.getType() != 10){
          return;
        }
        button.setVisible(true);
        button.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            OdeAsyncCallback<Void> updateCallback = new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.serverUnavailable()) {
              @Override
              public void onSuccess(final Void callBack) {
                button.setVisible(true);
              }
            };
            Ode.getInstance().getGalleryService().updateDatabaseField(updateCallback);
          }
        });
      }
    };
    Ode.getInstance().getUserInfoService().getUserInformation(callback);
    panel.add(button);
  }
  private class TemplateAction implements Command {
    TextArea msgText;
    int type;
    String customText;
    TemplateAction(TextArea msgText, int type, String customText){
      this.msgText = msgText;
      this.type = type;
      this.customText = customText;
    }
    @Override
    public void execute() {
      if(type == 1){
        msgText.setText("Your app \"" + customText  + "\" has been removed from the gallery due to inappropriate content. "
                + "Please review the guidelines at ..."
                + "If you feel this action has been taken in error, or you would like to discuss the issue, "
                + "please use the App Inventor forum at: \n");
        templateButton.setCaption("Inappropriate App Content");
      }else if(type == 2){
        msgText.setText("Your profile contains inappropriate content. Please modify your profile.\n");
        templateButton.setCaption("Inappropriate User profile content");
      }
    }
  }
}