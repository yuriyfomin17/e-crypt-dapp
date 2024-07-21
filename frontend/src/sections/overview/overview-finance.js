import PropTypes from 'prop-types';
import { Avatar, Button, Card, CardContent, Stack, SvgIcon, Typography } from '@mui/material';
import { Icon } from '@iconify/react';
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn';
import {
  CRYPTO_TRANSACTIONS_REST_PATH,
  ROLES,
  TOAST_GENERAL_ERROR_MESSAGE,
  WITHDRAW_CONFIRMATION_MESSAGE
} from '../../helpers/helper-constants';
import {
  ERROR_ICON, getMaxBalance, getTokenSalt,
  getUserRole, setMaxBalanceToZero, toastMessage,
  updateBalanceInfo
} from '../../helpers/helper-functions';
import {useAccount, useSignMessage} from 'wagmi';
import RotateRightIcon from '@mui/icons-material/RotateRight';
import {getUserBalance, UserTransactions, withdrawFunds} from '../../helpers/helper-rest-functions';

const BALANCE = 'BALANCE';
const PROFIT = 'PROFIT';
const isZeroBalance = (balance) => {
  if (!balance) {
    return false;
  }
  for (let i = 0; i < balance.length; i++) {
    if (balance[i] === '.' || balance[i] === '0') {
      continue;
    }
    return false;
  }
  return true;
};
export const OverviewFinance = ({
  type,
  ethUsdPrice,
  sx,
  ethBalance: devBalance,
  setIsNewGameFormOpened,
  setTopForm
}) => {
  const {isConnected} = useAccount();
  const role = getUserRole()
  const gameButtonTitle = role === ROLES.ROLE_USER ? '+Add game' : 'Create Game';
  const withdrawDisabled = isZeroBalance(devBalance);
  const tokenSalt = getTokenSalt()

  const { txMutator } = UserTransactions(CRYPTO_TRANSACTIONS_REST_PATH);


  const { signMessage, isLoading } = useSignMessage({
    message: WITHDRAW_CONFIRMATION_MESSAGE + tokenSalt,
    async onSuccess(signature) {
      const maxEthBalance = getMaxBalance()
      setMaxBalanceToZero()
      await withdrawFunds( signature, maxEthBalance)
          .catch(() => {
            toastMessage(TOAST_GENERAL_ERROR_MESSAGE, ERROR_ICON)
          });
      await txMutator()
    }
  });

  const updateUserBalance = async () => {
    const userData = await getUserBalance()
    if (userData){
      const {ethBalance, totalEthProfitEarned} = userData
      updateBalanceInfo({ethBalance, totalEthProfitEarned})
    }
  }

  const withDrawMaxBalance = async () => {
    await signMessage();
  }

  return (
    <Card sx={sx}>
      <CardContent>
        <Stack
          alignItems="flex-start"
          direction="row"
          justifyContent="space-between"
          spacing={3}
        >
          <Stack spacing={1}>
            <Typography
              color="text.secondary"
              variant="overline"
            >
              {type === BALANCE ? 'Balance' : 'Total Profit Earned'}
            </Typography>
            <Typography variant="h5">
              DEV {devBalance}
            </Typography>
          </Stack>
          <Avatar
            sx={{
              backgroundColor: 'success.main',
              height: 56,
              width: 56
            }}
          >
            <SvgIcon>
              {type === BALANCE ? <Icon icon="mdi:ethereum"/> : <MonetizationOnIcon/>}
            </SvgIcon>
          </Avatar>
        </Stack>
        {ethUsdPrice && (
          <Stack
            alignItems="center"
            direction="row"
            spacing={2}
            sx={{ mt: 2 }}
          >
            <Stack
              alignItems="center"
              direction="row"
              spacing={0.5}
            >
              <Typography
                color="success.main"
                variant="h6"
              >
                ≈ {ethUsdPrice} USD
              </Typography>
            </Stack>
          </Stack>

        )}
        {type === BALANCE && (
          <Stack
            alignItems="center"
            direction="row"
            spacing={2}
            sx={{ mt: 2 }}
          >
            <Stack
              alignItems="center"
              direction="row"
              spacing={1}
            >
              <Button
                variant="contained"
                onClick={setTopForm}
                disabled={!isConnected}
              >
                Top Up
              </Button>
              <Button
                variant="contained"
                disabled={!isConnected || withdrawDisabled}
                onClick={withDrawMaxBalance}
              >
                Withdraw Max Balance
              </Button>
              <Button
                startIcon={<RotateRightIcon/>}
                variant="contained"
                disabled={isLoading}
                onClick={updateUserBalance}
              >
                Sync
              </Button>
            </Stack>
          </Stack>
        )}

        {type === PROFIT && (
          <Stack
            alignItems="center"
            direction="row"
            spacing={2}
            sx={{ mt: 2 }}
          >
            <Stack
              alignItems="center"
              direction="row"
              spacing={1}
            >
              <Button
                variant="contained"
                onClick={() => setIsNewGameFormOpened(true)}
              >
                {gameButtonTitle}
              </Button>
            </Stack>
          </Stack>
        )}

      </CardContent>
    </Card>
  );
};

OverviewFinance.prototypes = {
  difference: PropTypes.number,
  positive: PropTypes.bool,
  sx: PropTypes.object,
  ethBalance: PropTypes.string.isRequired,
  setIsNewGameFormOpened: PropTypes.func.isRequired
};
