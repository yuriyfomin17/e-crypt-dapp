import TextField from '@mui/material/TextField';
import { convertToUsdt, getUserRole } from '../helpers/helper-functions';
import { Unstable_Grid2 as Grid } from '@mui/material';
import { ROLES } from '../helpers/helper-constants';
import { EthUsdPrice } from '../helpers/helper-rest-functions';

export const GameInputField = ({
  title,
  setBetId,
  setTitle,
  ethTicketPrice,
  setEthTicketPrice,
  betId
}) => {
  const role = getUserRole()
  const textField = (onChangeFunc,
    textFieldId,
    helperText,
    label,
    name,
    currentValue,
    valueType) => {
    return <TextField sx={{
      marginBottom: '10px'
    }}
                      type={valueType}
                      fullWidth
                      id={textFieldId}
                      helperText={helperText}
                      label={label}
                      name={name}
                      onChange={(e) => onChangeFunc(e)}
                      required={true}
                      value={currentValue}
    />;
  };
  const { ethUsdPrice } = EthUsdPrice();
  if (role === ROLES.ROLE_ADMIN) {
    return <Grid
      container
      spacing={3}
    >

      <Grid
        xs={12}
        md={12}
      >
        {textField((e) => {
            e.preventDefault();
            setTitle(e.target.value);
          },
          'titleId',
          '',
          'Please specify the title of the bet',
          'titleName',
          title,
          'text'
        )}
        {textField(e => {
            e.preventDefault();
            setEthTicketPrice(e.target.value);
          },
          'amount',
          '',
          'Ticket Price. Input ETH. Approx USD: ' + convertToUsdt(ethTicketPrice, ethUsdPrice),
          'ticketPrice',
          ethTicketPrice,
          'number'
        )}
      </Grid>
    </Grid>;
  }
  if (role === ROLES.ROLE_USER) {
    return <Grid
      container
      spacing={3}
    >
      <Grid
        xs={12}
        md={12}
      >
        {textField((e) => {
            e.preventDefault();
            setBetId(e.target.value);
          },

          'gameIdId',
          '',
          'Game ID',
          'gameIdName',
          betId,
          'number'
        )}
      </Grid>
    </Grid>;

  }
  return <></>;
};