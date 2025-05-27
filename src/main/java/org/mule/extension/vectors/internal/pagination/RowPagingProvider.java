package org.mule.extension.vectors.internal.pagination;

import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.StoreResponseAttributes;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.OperationValidator;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createPageStoreResponse;

public class RowPagingProvider implements PagingProvider<BaseStoreConnection, Result<CursorProvider, StoreResponseAttributes>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RowPagingProvider.class);

  private Iterator<BaseStore.Row<?>> rowIterator;
  private StoreConfiguration storeConfiguration;
  private String storeName;
  private QueryParameters queryParams;
  private StreamingHelper streamingHelper;
  private BaseStore baseStore;

  public RowPagingProvider(StoreConfiguration storeConfiguration,
                           String storeName,
                           QueryParameters queryParams,
                           StreamingHelper streamingHelper) {

    this.storeConfiguration = storeConfiguration;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.streamingHelper = streamingHelper;
  }

  @Override
  public List<Result<CursorProvider, StoreResponseAttributes>> getPage(BaseStoreConnection storeConnection) {

    try {

      if(baseStore == null) {

        OperationValidator.validateOperationType(
            Constants.STORE_OPERATION_TYPE_QUERY_ALL, storeConnection.getVectorStore());

        baseStore =  BaseStore.builder()
          .storeName(storeName)
          .configuration(storeConfiguration)
          .connection(storeConnection)
          .queryParams(queryParams)
          .createStore(false)
          .build();

        rowIterator = baseStore.rowIterator();
      }

      while(rowIterator.hasNext()) {

        try {

          BaseStore.Row<?> row = rowIterator.next();

          if(row == null) continue; // Skip null media

          JSONObject jsonObject = JsonUtils.rowToJson(row);

          return createPageStoreResponse(
              jsonObject.toString(),
              new HashMap<String, Object>() {{
                put("storeName", storeName);
              }},
              streamingHelper);

        } catch (Exception e) {

          // Look for next page if any on error
          LOGGER.warn(String.format("Error while getting row from %s. Trying next page.", storeName));
          e.printStackTrace();
        }

      }

    } catch (ModuleException me) {
      throw me;

    } catch (UnsupportedOperationException e) {

      LOGGER.debug(e.getMessage());
      throw new ModuleException(
          e.getMessage(),
          MuleVectorsErrorType.STORE_UNSUPPORTED_OPERATION);

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while getting row from %s.", this.storeName),
          MuleVectorsErrorType.STORE_SERVICES_FAILURE,
          e);
    }

    return new LinkedList<>();
  }

  @Override
  public Optional<Integer> getTotalResults(BaseStoreConnection baseStoreConnection) { return java.util.Optional.empty(); }

  @Override
  public boolean useStickyConnections() {
    return true;
  }

  @Override
  public void close(BaseStoreConnection baseStoreConnection) throws MuleException {

  }
}
