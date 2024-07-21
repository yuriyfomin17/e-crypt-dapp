import { Button, Stack } from '@mui/material';
import {
  changeAdminBetStatus,
  removeBetFromUserUpdateCache
} from '../../helpers/helper-rest-functions';
import { AdminFooterWrap } from './admin-footer-wrap';
import {
  BET_OUTCOME,
  BET_STATUSES
} from '../../helpers/helper-constants';
import { useState } from 'react';

const buttonStatusTranslator = (status) => {
  switch (status) {
    case BET_STATUSES.CREATED:
      return 'Start Game';
    case BET_STATUSES.OPENED:
      return 'Finish Game';
    case BET_STATUSES.RESOLVED:
      return 'Remove Game';
    default:
      return 'Not determined';
  }
};
export const AdminBetCardFooter = ({
  currentBet
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const { status, id, estimatedEthAdminProfit } = currentBet;

  const unlinkBetFromUserUpdateTransactions = async () => {
    await removeBetFromUserUpdateCache(id);
  };

  const startGame = async () => {
    if (status === BET_STATUSES.CREATED) {
      setIsLoading(true);
      await changeAdminBetStatus({
        id: id,
        status: BET_STATUSES.OPENED,
        outcome: BET_OUTCOME.NOT_DETERMINED
      });
      setIsLoading(false);
    }
  };

  const resolveBet = async (betOutcome) => {
    if (status === BET_STATUSES.CLOSED) {
      setIsLoading(true);
      await changeAdminBetStatus({
        id,
        status: BET_STATUSES.RESOLVED,
        outcome: betOutcome
      });
      setIsLoading(false);
    }
  };

  const finishBet = async () => {
    setIsLoading(true);
    await changeAdminBetStatus({
      id,
      status: BET_STATUSES.CLOSED,
      outcome: BET_OUTCOME.NOT_DETERMINED
    });
    setIsLoading(false);
  };

  switch (status) {
    case BET_STATUSES.CREATED:
      return <AdminFooterWrap estimatedEthAdminProfit={estimatedEthAdminProfit}>
        <Stack
          alignItems="center"
          direction="row"
          spacing={1}
        >
          <Stack
            alignItems="center"
            direction="row"
            justifyContent="space-between"
            spacing={2}
            sx={{ p: 2 }}
          >
            <Stack
              alignItems="center"
              direction="row"
              spacing={1}
            >
              <Button variant="contained"
                      color="success"
                      onClick={startGame}
                      disabled={isLoading}
              >{buttonStatusTranslator(status)}</Button>
            </Stack>
          </Stack>
        </Stack>
      </AdminFooterWrap>;

    case BET_STATUSES.CLOSED:
      return <AdminFooterWrap estimatedEthAdminProfit={estimatedEthAdminProfit}>
        <Stack
          justifyContent="space-between"
          direction="row"
          display="flex"
          spacing={1}
        >
          <Button variant="contained"
                  color="success"
                  disabled={isLoading}
                  onClick={() => resolveBet(BET_OUTCOME.WIN)}>
            YES
          </Button>
          <Button variant="contained"
                  color="error"
                  disabled={isLoading}
                  onClick={() => resolveBet(BET_OUTCOME.LOSE)}>
            NO
          </Button>
        </Stack>
      </AdminFooterWrap>;
    case BET_STATUSES.OPENED:
      return <AdminFooterWrap estimatedEthAdminProfit={estimatedEthAdminProfit}>
        <Button
          onClick={finishBet}
          variant="contained"
          color="success"
          disabled={isLoading}
        >
          {buttonStatusTranslator(status)}
        </Button>
      </AdminFooterWrap>;

    case BET_STATUSES.RESOLVED:
      return <AdminFooterWrap estimatedEthAdminProfit={estimatedEthAdminProfit}>
        <Button variant="contained"
                color="success"
                onClick={unlinkBetFromUserUpdateTransactions}
                disabled={isLoading}
        >{buttonStatusTranslator(status)}</Button>
      </AdminFooterWrap>;
    default:
      return <></>;
  }
};