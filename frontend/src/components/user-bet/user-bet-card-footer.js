import {
  EthUsdPrice,
  removeBetFromUserUpdateCache
} from '../../helpers/helper-rest-functions';
import { Box, Button, Divider, Typography } from '@mui/material';
import { UserFooterWrap } from './user-footer-wrap';
import { BET_STATUSES } from '../../helpers/helper-constants';
import {
  convertToUsdt,
} from '../../helpers/helper-functions';

export const UserBetCardFooter = ({
  currentBet,
  setIsBettingFormOpen,
  setBetOutcome
}) => {
  const { ethUsdPrice } = EthUsdPrice();
  const {
    id,
    status,
    betPlaced
  } = currentBet;
  const handleYesBetOutcome = async () => {
    setBetOutcome(true);
    setIsBettingFormOpen(true);
  };
  const handleNoBetOutcome = () => {
    setBetOutcome(false);
    setIsBettingFormOpen(true);
  };
  const unlinkBetFromUserUpdateGamesTransactions = async () => {
    await removeBetFromUserUpdateCache(id);
  };
  switch (status) {
    case BET_STATUSES.OPENED:
      if (betPlaced) {
        return <>
          <Divider color="grey"
                   sx={{ height: 2, width: '100%' }}/>
          <UserFooterWrap>
            <Box sx={{
              p: 2,
              width: '100%',
              borderRadius: '10px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'start'
            }}>
              <Typography color="text.primary"
                          variant="h6">
                Placed {currentBet.ethTicketPrice} ETH
                ≈ {convertToUsdt(currentBet.ethTicketPrice, ethUsdPrice)} usd
              </Typography>
            </Box>
          </UserFooterWrap>
        </>;

      } else {
        return <>
          <Divider color="grey"
                   sx={{ height: 2, width: '100%' }}/>
          <UserFooterWrap>
            <Box sx={{
              p: 2,
              width: '100%'
            }}>
              <Button variant="contained"
                      fullWidth
                      onClick={handleYesBetOutcome}
              >
                Predict Yes
              </Button>
            </Box>
            <Box sx={{
              p: 2,
              width: '100%',
              borderRadius: '10px',
              display: 'flex',
              justifyContent: 'space-between'
            }}>
              <Button variant="contained"
                      fullWidth
                      onClick={handleNoBetOutcome}
              >
                Predict No
              </Button>
            </Box>
          </UserFooterWrap>
        </>;
      }

    case BET_STATUSES.CLOSED:
      return <>
        <Divider color="grey"
                 sx={{ height: 2, width: '100%' }}/>
        <Box sx={{
          p: 2,
          width: '100%'
        }}>
          <Typography color="text.secondary">
            Outcome will be determined by admin soon
          </Typography>
        </Box>
      </>;

    case BET_STATUSES.RESOLVED:
      return <Box sx={{
        p: 2,
        width: '100%',
        borderRadius: '10px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Button variant="contained"
                  onClick={unlinkBetFromUserUpdateGamesTransactions}>
            Remove Game</Button>
        </Box>
      </Box>;
    default:
      return <></>;
  }

};