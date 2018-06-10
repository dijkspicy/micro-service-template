package dijkspicy.queeng.server.dispatch.connector;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/29
 */
public class ConnectorAQLService implements org.apache.calcite.avatica.remote.Service {
    @Override
    public ResultSetResponse apply(CatalogsRequest catalogsRequest) {
        return null;
    }

    @Override
    public ResultSetResponse apply(SchemasRequest schemasRequest) {
        return null;
    }

    @Override
    public ResultSetResponse apply(TablesRequest tablesRequest) {
        return null;
    }

    @Override
    public ResultSetResponse apply(TableTypesRequest tableTypesRequest) {
        return null;
    }

    @Override
    public ResultSetResponse apply(TypeInfoRequest typeInfoRequest) {
        return null;
    }

    @Override
    public ResultSetResponse apply(ColumnsRequest columnsRequest) {
        return null;
    }

    @Override
    public PrepareResponse apply(PrepareRequest prepareRequest) {
        return null;
    }

    @Override
    public ExecuteResponse apply(ExecuteRequest executeRequest) {
        return null;
    }

    @Override
    public ExecuteResponse apply(PrepareAndExecuteRequest prepareAndExecuteRequest) {
        return null;
    }

    @Override
    public SyncResultsResponse apply(SyncResultsRequest syncResultsRequest) {
        return null;
    }

    @Override
    public FetchResponse apply(FetchRequest fetchRequest) {
        return null;
    }

    @Override
    public CreateStatementResponse apply(CreateStatementRequest createStatementRequest) {
        return null;
    }

    @Override
    public CloseStatementResponse apply(CloseStatementRequest closeStatementRequest) {
        return null;
    }

    @Override
    public OpenConnectionResponse apply(OpenConnectionRequest openConnectionRequest) {
        return null;
    }

    @Override
    public CloseConnectionResponse apply(CloseConnectionRequest closeConnectionRequest) {
        return null;
    }

    @Override
    public ConnectionSyncResponse apply(ConnectionSyncRequest connectionSyncRequest) {
        return null;
    }

    @Override
    public DatabasePropertyResponse apply(DatabasePropertyRequest databasePropertyRequest) {
        return null;
    }

    @Override
    public CommitResponse apply(CommitRequest commitRequest) {
        return null;
    }

    @Override
    public RollbackResponse apply(RollbackRequest rollbackRequest) {
        return null;
    }

    @Override
    public ExecuteBatchResponse apply(PrepareAndExecuteBatchRequest prepareAndExecuteBatchRequest) {
        return null;
    }

    @Override
    public ExecuteBatchResponse apply(ExecuteBatchRequest executeBatchRequest) {
        return null;
    }

    @Override
    public void setRpcMetadata(RpcMetadataResponse rpcMetadataResponse) {

    }
}
