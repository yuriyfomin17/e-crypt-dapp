import PropTypes from 'prop-types';
import {
  Card,
  CardHeader,
  IconButton, Stack,
  SvgIcon,
  Typography
} from '@mui/material';
import Tooltip from '@mui/material/Tooltip';
import { EthUsdPrice } from '../../helpers/helper-rest-functions';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import { useState } from 'react';
import DoneIcon from '@mui/icons-material/Done';
import { BetCardContent } from '../../components/bet-card-content';
import { AdminBetCardFooter } from '../../components/admin-bet/admin-bet-card-footer';
import { convertToUsdt } from '../../helpers/helper-functions';

export const AdminBetCard = ({ currentBet }) => {
  const { id, title, ethTicketPrice } = currentBet;
  const [copied, setCopied] = useState(false);
  const { ethUsdPrice } = EthUsdPrice();

  const copyBetId = () => {
    navigator.clipboard.writeText(id).then().catch();
    setCopied(!copied);
  };

  return (
    <Card
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        border: '1px solid',
        borderColor: 'neutral.400'
      }}
    >
      <CardHeader sx={{ alignSelf: 'center', marginBottom: '5px' }}
                  titleTypographyProps={{ textAlign: 'center' }}
                  title={title}
                  subheader={<Stack
                    alignItems="center"
                    justifyContent="center">
                    <Stack direction="row"
                           alignItems="center"
                           justifyContent="center">
                      <IconButton size="small"
                                  onClick={copyBetId}>
                        <Tooltip title="Copy Bet ID">
                          <SvgIcon
                            color="action"
                            fontSize="small"
                          >
                            {copied ? <DoneIcon/> : <BadgeOutlinedIcon/>}
                          </SvgIcon>
                        </Tooltip>
                      </IconButton>
                      <Typography color="text.secondary"
                                  variant="text.secondary">
                        {id}
                      </Typography>
                    </Stack>
                    <Typography>
                      {'Ticket Price:' + ethTicketPrice + ' ETH' + ' ≈ ' + convertToUsdt(
                        ethTicketPrice,
                        ethUsdPrice) + 'usd'}
                    </Typography>
                  </Stack>}
                  subheaderTypographyProps={{ textAlign: 'center' }}
      />
      <BetCardContent currentBet={currentBet}/>
      <AdminBetCardFooter currentBet={currentBet} />
    </Card>
  );
};

AdminBetCard.propTypes = {
  currentBet: PropTypes.object.isRequired
};