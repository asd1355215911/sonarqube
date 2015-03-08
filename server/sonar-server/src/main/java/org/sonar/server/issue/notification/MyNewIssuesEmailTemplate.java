/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.issue.notification;

import org.sonar.api.config.EmailSettings;
import org.sonar.api.i18n.I18n;
import org.sonar.api.notifications.Notification;
import org.sonar.api.utils.DateUtils;
import org.sonar.server.issue.notification.NewIssuesStatistics.METRIC;

import java.util.Date;

/**
 * Creates email message for notification "my-new-issues".
 */
public class MyNewIssuesEmailTemplate extends AbstractNewIssuesEmailTemplate {

  public MyNewIssuesEmailTemplate(EmailSettings settings, I18n i18n) {
    super(settings, i18n);
  }

  @Override
  protected boolean shouldNotFormat(Notification notification) {
    return !MyNewIssuesNotification.TYPE.equals(notification.getType());
  }

  @Override
  protected void appendAssignees(StringBuilder message, Notification notification) {
    // do nothing as we don't want to print assignees, it's a personalized email for one person
  }

  @Override
  protected String subject(Notification notification, String projectName) {
    return String.format("You have %s new issues on project %s",
      notification.getFieldValue(METRIC.SEVERITY + COUNT),
      projectName);
  }

  @Override
  protected void appendFooter(StringBuilder message, Notification notification) {
    String projectUuid = notification.getFieldValue(FIELD_PROJECT_UUID);
    String dateString = notification.getFieldValue(FIELD_PROJECT_DATE);
    String assignee = notification.getFieldValue(FIELD_ASSIGNEE);
    if (projectUuid != null && dateString != null && assignee != null) {
      Date date = DateUtils.parseDateTime(dateString);
      String url = String.format("%s/issues/search#projectUuids=%s|createdAt=%s|assignees=%s",
        settings.getServerBaseURL(),
        encode(projectUuid),
        encode(DateUtils.formatDateTime(date)),
        encode(assignee));
      message.append(NEW_LINE).append("See it in SonarQube: ").append(url).append(NEW_LINE);
    }
  }
}