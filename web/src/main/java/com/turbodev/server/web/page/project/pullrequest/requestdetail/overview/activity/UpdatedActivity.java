package com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.turbodev.server.TurboDev;
import com.turbodev.server.model.PullRequestUpdate;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class UpdatedActivity implements PullRequestActivity {

	private final Long updateId;
	
	public UpdatedActivity(PullRequestUpdate update) {
		updateId = update.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new UpdatedPanel(panelId, new LoadableDetachableModel<PullRequestUpdate>() {

			@Override
			protected PullRequestUpdate load() {
				return getUpdate();
			}
			
		});
	}

	public PullRequestUpdate getUpdate() {
		return TurboDev.getInstance(Dao.class).load(PullRequestUpdate.class, updateId);
	}

	@Override
	public Date getDate() {
		return getUpdate().getDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

}