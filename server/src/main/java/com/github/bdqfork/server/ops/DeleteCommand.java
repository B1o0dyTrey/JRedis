package com.github.bdqfork.server.ops;

import com.github.bdqfork.server.transaction.OperationType;

/**
 * @author Trey
 * @since 2021/1/7
 */
public abstract class DeleteCommand implements Command {

  @Override
  public OperationType getOperationType() {
    return OperationType.DELETE;
  }

}
