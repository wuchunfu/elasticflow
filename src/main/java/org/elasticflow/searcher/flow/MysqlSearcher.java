package org.elasticflow.searcher.flow;

import org.elasticflow.model.searcher.SearcherModel;
import org.elasticflow.model.searcher.SearcherResult;
import org.elasticflow.param.pipe.ConnectParams;
import org.elasticflow.searcher.SearcherFlowSocket;
import org.elasticflow.searcher.handler.SearcherHandler;
import org.elasticflow.util.EFException;

/**
 * Main Run Class for Searcher
 * @author chengwen
 * @version 2.0
 * @date 2018-10-26 09:23
 */
public class MysqlSearcher extends SearcherFlowSocket{

	public static MysqlSearcher getInstance(ConnectParams connectParams) {
		return null;
	}

	@Override
	public SearcherResult Search(SearcherModel<?, ?> searcherModel, String instance, SearcherHandler handler) throws EFException {
		// TODO Auto-generated method stub
		return null;
	}
}
